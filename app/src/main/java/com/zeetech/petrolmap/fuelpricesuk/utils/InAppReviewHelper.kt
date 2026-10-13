package com.zeetech.petrolmap.fuelpricesuk.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import com.zeetech.petrolmap.fuelpricesuk.data.local.PreferencesManager

object InAppReviewHelper {
    private const val TAG = "InAppReview"

    /**
     * Checks if the user is eligible for an automatic review prompt (>=24 hours since first launch,
     * >=24 hours since last prompt, not yet rated).
     * Follows Google Play In-App Review guidelines: does not gate, screen, or alter user flow.
     */
    fun checkAndPromptReviewIfEligible(activity: Activity, prefs: PreferencesManager) {
        if (!prefs.shouldPromptInAppReview()) {
            return
        }

        Log.d(TAG, "User is eligible for In-App Review prompt. Requesting flow...")
        val reviewManager: ReviewManager = ReviewManagerFactory.create(activity)
        val requestTask = reviewManager.requestReviewFlow()

        requestTask.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                val reviewInfo = request.result
                prefs.markReviewPrompted()

                val flowTask = reviewManager.launchReviewFlow(activity, reviewInfo)
                flowTask.addOnCompleteListener { _ ->
                    // The flow has finished. The API doesn't indicate whether the user
                    // reviewed or cancelled, as per Google Play privacy guidelines.
                    Log.d(TAG, "In-App review flow completed successfully.")
                    Firebase.analytics.logEvent("in_app_review_completed", null)
                }
            } else {
                Log.e(TAG, "Failed requesting in-app review flow", request.exception)
                prefs.markReviewPrompted() // Throttle for 24h to avoid spamming failed calls
                request.exception?.let { Firebase.crashlytics.recordException(it) }
            }
        }
    }

    /**
     * Direct review trigger (e.g. user explicitly taps "Rate Us" in settings).
     */
    fun launchDirectReview(activity: Activity, prefs: PreferencesManager) {
        prefs.markUserRated()
        Firebase.analytics.logEvent("user_clicked_rate_us", null)

        val reviewManager: ReviewManager = ReviewManagerFactory.create(activity)
        val requestTask = reviewManager.requestReviewFlow()

        requestTask.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                val reviewInfo = request.result
                reviewManager.launchReviewFlow(activity, reviewInfo)
                    .addOnFailureListener {
                        openPlayStorePage(activity)
                    }
            } else {
                openPlayStorePage(activity)
            }
        }
    }

    private fun openPlayStorePage(activity: Activity) {
        val packageName = activity.packageName
        try {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            )
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}
