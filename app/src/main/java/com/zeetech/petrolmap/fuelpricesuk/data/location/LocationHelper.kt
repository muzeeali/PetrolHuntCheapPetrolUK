package com.zeetech.petrolmap.fuelpricesuk.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.zeetech.petrolmap.fuelpricesuk.data.local.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    fun hasNotificationPermission(context: Context): Boolean {
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!notificationsEnabled) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isLocationPermanentlyDenied(activity: Activity?, prefs: PreferencesManager): Boolean {
        if (activity == null) return false
        if (hasLocationPermission(activity)) return false
        if (!prefs.hasRequestedLocationPermission) return false
        return !ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun isNotificationPermanentlyDenied(activity: Activity?, prefs: PreferencesManager): Boolean {
        if (activity == null) return false
        val systemNotificationsEnabled = NotificationManagerCompat.from(activity).areNotificationsEnabled()
        if (!systemNotificationsEnabled) return true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasNotificationPermission(activity)) return false
            if (!prefs.hasRequestedNotificationPermission) return false
            return !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        return false
    }

    fun openAppNotificationSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                openAppSettings(context)
            }
        } catch (_: Exception) {
            openAppSettings(context)
        }
    }

    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun isLocationServiceEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun promptEnableGps(
        activity: Activity,
        onResolvable: (IntentSenderRequest) -> Unit,
        onAlreadyEnabled: () -> Unit,
        onFallback: () -> Unit
    ) {
        if (isLocationServiceEnabled(activity)) {
            onAlreadyEnabled()
            return
        }

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

            val client = LocationServices.getSettingsClient(activity)
            client.checkLocationSettings(builder.build())
                .addOnSuccessListener {
                    onAlreadyEnabled()
                }
                .addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                            onResolvable(intentSenderRequest)
                        } catch (e: Exception) {
                            onFallback()
                        }
                    } else {
                        onFallback()
                    }
                }
        } catch (e: Exception) {
            onFallback()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? = withContext(Dispatchers.Main) {
        if (!hasLocationPermission(context)) return@withContext null

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        // 1. Instant check: Best last known location across all available providers
        var bestLastKnown: Location? = null
        if (lm != null) {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            for (provider in providers) {
                if (lm.isProviderEnabled(provider)) {
                    val loc = try { lm.getLastKnownLocation(provider) } catch (e: Exception) { null }
                    if (loc != null && (bestLastKnown == null || loc.time > bestLastKnown.time)) {
                        bestLastKnown = loc
                    }
                }
            }
        }

        // Try Google Play Services Fused Client last location as well
        val fusedLastLoc = try {
            withTimeoutOrNull(800) {
                suspendCancellableCoroutine<Location?> { cont ->
                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedClient.lastLocation
                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                }
            }
        } catch (e: Exception) {
            null
        }

        if (fusedLastLoc != null && (bestLastKnown == null || fusedLastLoc.time > bestLastKnown.time)) {
            bestLastKnown = fusedLastLoc
        }

        // If we have a fresh last known location (within last 3 minutes), return it immediately
        val threeMinutesAgo = System.currentTimeMillis() - (3 * 60 * 1000)
        if (bestLastKnown != null && bestLastKnown.time > threeMinutesAgo) {
            return@withContext bestLastKnown
        }

        // 2. Request fresh location update from FusedLocationProviderClient & LocationManager concurrently
        val freshLocation = try {
            withTimeoutOrNull(4500) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    val isCompleted = java.util.concurrent.atomic.AtomicBoolean(false)
                    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                    val cts = CancellationTokenSource()
                    val listeners = java.util.Collections.synchronizedList(mutableListOf<LocationListener>())

                    fun cleanup() {
                        cts.cancel()
                        if (lm != null) {
                            synchronized(listeners) {
                                for (l in listeners) {
                                    try { lm.removeUpdates(l) } catch (_: Exception) {}
                                }
                                listeners.clear()
                            }
                        }
                    }

                    // A. Fused Client High Accuracy request
                    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { loc ->
                            if (isCompleted.compareAndSet(false, true)) {
                                cleanup()
                                if (continuation.isActive) {
                                    continuation.resume(loc)
                                }
                            }
                        }
                        .addOnFailureListener {
                            // Fused failed or unavailable
                        }

                    // B. Native LocationManager concurrent listeners (Network + GPS)
                    if (lm != null) {
                        val activeProviders = mutableListOf<String>()
                        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) activeProviders.add(LocationManager.NETWORK_PROVIDER)
                        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) activeProviders.add(LocationManager.GPS_PROVIDER)

                        for (prov in activeProviders) {
                            val listener = object : LocationListener {
                                override fun onLocationChanged(location: Location) {
                                    if (isCompleted.compareAndSet(false, true)) {
                                        cleanup()
                                        if (continuation.isActive) {
                                            continuation.resume(location)
                                        }
                                    }
                                }
                                @Deprecated("Deprecated in Java")
                                override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                                override fun onProviderEnabled(p: String) {}
                                override fun onProviderDisabled(p: String) {}
                            }
                            listeners.add(listener)
                            try {
                                lm.requestLocationUpdates(prov, 0L, 0f, listener, Looper.getMainLooper())
                            } catch (e: Exception) {
                                // Ignore provider registration failure
                            }
                        }
                    }

                    continuation.invokeOnCancellation {
                        cleanup()
                    }
                }
            }
        } catch (e: Exception) {
            null
        }

        freshLocation ?: bestLastKnown
    }
}
