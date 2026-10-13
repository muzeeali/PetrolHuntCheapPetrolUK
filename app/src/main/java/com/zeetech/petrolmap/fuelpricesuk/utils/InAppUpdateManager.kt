package com.zeetech.petrolmap.fuelpricesuk.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class InAppUpdateManager(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var installStateUpdatedListener: InstallStateUpdatedListener? = null
    var onFlexibleUpdateDownloaded: (() -> Unit)? = null

    fun checkAndStartUpdate() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour caching in production
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set in-app default: "immediate" as required
        remoteConfig.setDefaultsAsync(mapOf(KEY_APP_UPDATE_TYPE to DEFAULT_UPDATE_TYPE))

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                val updateTypeMode = remoteConfig.getString(KEY_APP_UPDATE_TYPE)
                    .trim()
                    .lowercase()

                Log.d(TAG, "Firebase Remote Config app_update_type: $updateTypeMode")
                handleUpdateMode(updateTypeMode)
            }
    }

    private fun handleUpdateMode(mode: String) {
        if (mode == "disabled" || mode == "disable") {
            Log.d(TAG, "In-app updates disabled via Remote Config")
            return
        }

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Firebase.analytics.logEvent("app_update_available") {
                    param("version_code", appUpdateInfo.availableVersionCode().toLong())
                    param("mode", mode)
                }

                when (mode) {
                    "flexible" -> {
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            startFlexibleUpdate(appUpdateInfo)
                        } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            startImmediateUpdate(appUpdateInfo)
                        }
                    }
                    else -> { // Default is "immediate"
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            startImmediateUpdate(appUpdateInfo)
                        } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            startFlexibleUpdate(appUpdateInfo)
                        }
                    }
                }
            }
        }.addOnFailureListener { error ->
            Log.e(TAG, "Failed checking app update availability", error)
            Firebase.crashlytics.recordException(error)
        }
    }

    private fun startImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlow(
                appUpdateInfo,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
            )
            Firebase.analytics.logEvent("app_update_started") {
                param("type", "immediate")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed starting immediate update flow", e)
            Firebase.crashlytics.recordException(e)
        }
    }

    private fun startFlexibleUpdate(appUpdateInfo: AppUpdateInfo) {
        registerInstallListener()
        try {
            appUpdateManager.startUpdateFlow(
                appUpdateInfo,
                activity,
                AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE)
            )
            Firebase.analytics.logEvent("app_update_started") {
                param("type", "flexible")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed starting flexible update flow", e)
            Firebase.crashlytics.recordException(e)
        }
    }

    private fun registerInstallListener() {
        if (installStateUpdatedListener == null) {
            installStateUpdatedListener = InstallStateUpdatedListener { state: InstallState ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    Log.d(TAG, "Flexible update downloaded successfully")
                    Firebase.analytics.logEvent("app_update_downloaded", null)
                    onFlexibleUpdateDownloaded?.invoke()
                }
            }
            installStateUpdatedListener?.let { appUpdateManager.registerListener(it) }
        }
    }

    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun onResumeCheck() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // Resume ongoing immediate update
                try {
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed resuming immediate update flow", e)
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // If downloaded in background, inform UI
                onFlexibleUpdateDownloaded?.invoke()
            }
        }
    }

    fun onDestroy() {
        installStateUpdatedListener?.let {
            appUpdateManager.unregisterListener(it)
        }
    }

    companion object {
        private const val TAG = "InAppUpdate"
        const val KEY_APP_UPDATE_TYPE = "app_update_type"
        const val DEFAULT_UPDATE_TYPE = "immediate"
    }
}
