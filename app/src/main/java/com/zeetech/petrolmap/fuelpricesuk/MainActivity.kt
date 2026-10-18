package com.zeetech.petrolmap.fuelpricesuk

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zeetech.petrolmap.fuelpricesuk.data.local.PreferencesManager
import com.zeetech.petrolmap.fuelpricesuk.data.local.UKGeocodingHelper
import com.zeetech.petrolmap.fuelpricesuk.data.location.LocationHelper
import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.data.model.NavigationApp
import com.zeetech.petrolmap.fuelpricesuk.data.model.SearchLocation
import com.zeetech.petrolmap.fuelpricesuk.data.model.SortMode
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import com.zeetech.petrolmap.fuelpricesuk.data.network.PetrolMapApiClient
import com.zeetech.petrolmap.fuelpricesuk.data.network.StationsResult
import com.zeetech.petrolmap.fuelpricesuk.ui.components.AppTab
import com.zeetech.petrolmap.fuelpricesuk.ui.components.BottomNavBar
import com.zeetech.petrolmap.fuelpricesuk.ui.components.TopHeaderBar
import com.zeetech.petrolmap.fuelpricesuk.data.location.NavigationHelper
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.DirectionsDialog
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.launchNavigation
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.FilterBottomSheet
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.FirstLaunchLocationScreen
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.FuelBottomSheet
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.MainMapScreen
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.SearchScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.SettingsScreen
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.SortBottomSheet
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.StationDetailDialog
import com.zeetech.petrolmap.fuelpricesuk.ui.screens.TurnOnGpsDialog
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMapFuelPricesUKTheme
import com.zeetech.petrolmap.fuelpricesuk.utils.InAppReviewHelper
import com.zeetech.petrolmap.fuelpricesuk.utils.InAppUpdateManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurface
import androidx.compose.ui.graphics.toArgb

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(
                PetrolSurface.toArgb()
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.dark(
                PetrolSurface.toArgb()
            )
        )
        setContent {
            PetrolMapFuelPricesUKTheme {
                PetrolMapApp()
            }
        }
    }
}

@Composable
fun PetrolMapApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferencesManager(context) }

    // Persistent Preferences
    var currentTab by remember { mutableStateOf(AppTab.MAP) }
    var selectedFuel by remember { mutableStateOf(prefs.fuelType) }
    var searchRadius by remember { mutableIntStateOf(prefs.searchRadius) }
    var sortMode by remember { mutableStateOf(prefs.sortMode) }
    var navigationApp by remember { mutableStateOf(prefs.navigationApp) }

    // Map & Location State (Default: saved GPS location or central UK fallback)
    val savedLat = prefs.lastUserLat
    val savedLng = prefs.lastUserLng
    var isUserPinnedLocation by remember { mutableStateOf(false) }
    var pinnedLat by remember { mutableStateOf<Double?>(null) }
    var pinnedLng by remember { mutableStateOf<Double?>(null) }

    var locationTitle by remember { mutableStateOf(if (savedLat != null) "My Location" else "London Centre") }
    var centerLat by remember { mutableDoubleStateOf(savedLat ?: 51.5074) }
    var centerLng by remember { mutableDoubleStateOf(savedLng ?: -0.1278) }

    var userLat by remember { mutableStateOf(savedLat) }
    var userLng by remember { mutableStateOf(savedLng) }
    var recenterTrigger by remember { mutableIntStateOf(0) }
    var isLocationGranted by remember { mutableStateOf(LocationHelper.hasLocationPermission(context)) }
    var isNotificationGranted by remember { mutableStateOf(LocationHelper.hasNotificationPermission(context)) }
    var fuelAlertsEnabledState by remember { mutableStateOf(prefs.fuelAlertsEnabled && LocationHelper.hasNotificationPermission(context)) }
    var isLocationDenied by remember { mutableStateOf(!LocationHelper.hasLocationPermission(context)) }
    var isGpsRequestedByUser by remember { mutableStateOf(false) }
    var isNotificationRequestedByUser by remember { mutableStateOf(false) }

    // Stations Data State
    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var minPrice by remember { mutableStateOf<Double?>(null) }
    var maxPrice by remember { mutableStateOf<Double?>(null) }
    var lastUpdatedTime by remember { mutableStateOf(prefs.lastUpdatedTime) }

    // Loading / Status State
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    var isOutsideUk by remember { mutableStateOf(false) }
    var mapDebounceJob by remember { mutableStateOf<Job?>(null) }
    var showLocationDisclosureDialog by remember { mutableStateOf(false) }
    var showNotificationDisclosureDialog by remember { mutableStateOf(false) }

    // Screen Overlays / Dialogs
    val activity = context as? Activity
    val inAppUpdateManager = remember(activity) { activity?.let { InAppUpdateManager(it) } }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(inAppUpdateManager) {
        inAppUpdateManager?.onFlexibleUpdateDownloaded = {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "An update has been downloaded.",
                    actionLabel = "Restart",
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.ActionPerformed) {
                    inAppUpdateManager.completeFlexibleUpdate()
                }
            }
        }
    }
    var showFirstLaunch by remember {
        mutableStateOf(!prefs.hasPromptedLocation && !LocationHelper.hasLocationPermission(context))
    }
    var showLocationAlreadyGrantedDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionRequiredDialog by remember { mutableStateOf(false) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    var showTurnOnGpsDialog by remember { mutableStateOf(false) }
    var showSearchScreen by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showFuelSheet by remember { mutableStateOf(false) }
    var showStationDetails by remember { mutableStateOf(false) }
    var showDirectionsDialog by remember { mutableStateOf(false) }
    var stationForDirections by remember { mutableStateOf<Station?>(null) }

    val handleOpenDirections: (Station) -> Unit = { st ->
        if (prefs.rememberNavigationAppChoice && NavigationHelper.isAppInstalled(context, prefs.navigationApp)) {
            launchNavigation(context, st, prefs.navigationApp)
        } else {
            stationForDirections = st
            showDirectionsDialog = true
        }
    }

    var recentSearches by remember { mutableStateOf(prefs.getRecentSearches()) }

    // Deterministic sorting helper
    fun sortStations(list: List<Station>, mode: SortMode, fuel: FuelType): List<Station> {
        return when (mode) {
            SortMode.CHEAPEST -> list.sortedWith(
                compareBy<Station> { it.price }.thenBy { it.distanceMiles }
            )
            SortMode.NEAREST -> list.sortedWith(
                compareBy<Station> { it.distanceMiles }.thenBy { it.price }
            )
            SortMode.LATEST -> list.sortedWith(
                compareByDescending<Station> { st ->
                    PetrolMapApiClient.parseTimestampToEpoch(st.fuelUpdatedDates[fuel.code] ?: st.updatedAt)
                }.thenBy { it.price }
            )
        }
    }

    // Function to fetch station prices
    fun fetchStations(isRefresh: Boolean = false) {
        scope.launch {
            if (!UKGeocodingHelper.isInsideUk(centerLat, centerLng)) {
                isOutsideUk = true
                stations = emptyList()
                minPrice = null
                maxPrice = null
                isOffline = false
                isLoading = false
                isRefreshing = false
                return@launch
            }

            if (isRefresh) isRefreshing = true else isLoading = true

            val queryLat = if (isUserPinnedLocation && pinnedLat != null) pinnedLat!! else centerLat
            val queryLng = if (isUserPinnedLocation && pinnedLng != null) pinnedLng!! else centerLng

            val result = PetrolMapApiClient.getNearbyStations(
                latitude = queryLat,
                longitude = queryLng,
                fuel = selectedFuel,
                radiusMiles = searchRadius,
                sort = sortMode
            )

            when (result) {
                is StationsResult.Success -> {
                    stations = sortStations(result.stations, sortMode, selectedFuel)
                    isOutsideUk = !result.isUk
                    val validPrices = result.stations.map { it.price }.filter { it > 0.0 }
                    minPrice = validPrices.minOrNull() ?: result.minPrice
                    maxPrice = validPrices.maxOrNull() ?: result.maxPrice
                    isOffline = false
                    if (result.isUk) {
                        lastUpdatedTime = result.fetchedAt
                        withContext(Dispatchers.IO) {
                            prefs.saveCachedStations(result.stations, result.fetchedAt)
                        }
                    }
                    try {
                        Firebase.analytics.logEvent("search_fuel_stations") {
                            param("fuel_type", selectedFuel.code)
                            param("radius_miles", searchRadius.toLong())
                            param("stations_count", result.stations.size.toLong())
                        }
                    } catch (e: Exception) {}
                }
                is StationsResult.Error -> {
                    isOffline = true
                    try {
                        Firebase.crashlytics.log("Fetch stations failed: ${result.message}")
                    } catch (e: Exception) {}
                    // Fall back to offline cached stations if available
                    val cached: List<Station> = withContext(Dispatchers.IO) {
                        prefs.getCachedStations()
                    }
                    if (cached.isNotEmpty()) {
                        stations = sortStations(cached, sortMode, selectedFuel)
                        val validPrices = cached.map { s: Station -> s.price }.filter { p: Double -> p > 0.0 }
                        minPrice = validPrices.minOrNull()
                        maxPrice = validPrices.maxOrNull()
                    }
                }
            }

            isLoading = false
            isRefreshing = false
        }
    }

    // Function to actively detect current GPS location
    fun detectLocationAndFetch(forceRecenter: Boolean = false) {
        scope.launch {
            isLoading = true
            if (!LocationHelper.isLocationServiceEnabled(context)) {
                android.widget.Toast.makeText(
                    context,
                    "Please turn on Location / GPS in your device settings",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }

            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                userLat = loc.latitude
                userLng = loc.longitude
                prefs.lastUserLat = loc.latitude
                prefs.lastUserLng = loc.longitude
                isLocationDenied = false

                if (!isUserPinnedLocation || forceRecenter) {
                    centerLat = loc.latitude
                    centerLng = loc.longitude
                    isOutsideUk = !UKGeocodingHelper.isInsideUk(loc.latitude, loc.longitude)
                    locationTitle = if (isOutsideUk) "My Location (Outside UK)" else "My Location"
                    recenterTrigger++
                }
            } else if (forceRecenter) {
                if (userLat != null && userLng != null) {
                    centerLat = userLat!!
                    centerLng = userLng!!
                    locationTitle = "My Location"
                    recenterTrigger++
                } else {
                    android.widget.Toast.makeText(
                        context,
                        "Acquiring satellite & network location fix...",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            fetchStations()
        }
    }

    // Google Play Services GPS Resolution Launcher
    val enableGpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || LocationHelper.isLocationServiceEnabled(context)) {
            showTurnOnGpsDialog = false
            detectLocationAndFetch(forceRecenter = true)
        } else {
            showTurnOnGpsDialog = true
        }
    }

    // Function to prompt user to enable GPS
    fun requestTurnOnGps() {
        if (activity != null) {
            LocationHelper.promptEnableGps(
                activity = activity,
                onResolvable = { intentSenderRequest ->
                    enableGpsLauncher.launch(intentSenderRequest)
                },
                onAlreadyEnabled = {
                    showTurnOnGpsDialog = false
                    detectLocationAndFetch(forceRecenter = true)
                },
                onFallback = {
                    try {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            context,
                            "Please enable Location in your device settings",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        } else {
            try {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {}
        }
    }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        prefs.hasRequestedNotificationPermission = true
        isNotificationGranted = isGranted
        prefs.fuelAlertsEnabled = isGranted
        fuelAlertsEnabledState = isGranted
        if (isGranted) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
            android.widget.Toast.makeText(context, "Fuel price alerts enabled", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
            if (isNotificationRequestedByUser && LocationHelper.isNotificationPermanentlyDenied(activity, prefs)) {
                showNotificationPermissionRequiredDialog = true
            }
        }
        isNotificationRequestedByUser = false
    }

    // Permission Launcher: Handles Location permissions
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fineGranted || coarseGranted
        prefs.hasPromptedLocation = true
        prefs.hasRequestedLocationPermission = true
        showFirstLaunch = false
        isLocationGranted = granted
        isLocationDenied = !granted

        if (granted) {
            showLocationPermissionDialog = false
            showLocationDisclosureDialog = false
            isGpsRequestedByUser = false
            if (!LocationHelper.isLocationServiceEnabled(context)) {
                // Once permission is granted, immediately prompt user to turn on GPS!
                showTurnOnGpsDialog = true
                requestTurnOnGps()
                fetchStations()
            } else {
                detectLocationAndFetch(forceRecenter = true)
            }
        } else {
            if (isGpsRequestedByUser && LocationHelper.isLocationPermanentlyDenied(activity, prefs)) {
                showLocationPermissionDialog = true
            }
            fetchStations()
        }

        // Prompt notification permission with disclosure on Android 13+ if not prompted before
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !LocationHelper.hasNotificationPermission(context) &&
            !prefs.hasRequestedNotificationPermission) {
            showNotificationDisclosureDialog = true
        } else if (LocationHelper.hasNotificationPermission(context)) {
            prefs.fuelAlertsEnabled = true
            fuelAlertsEnabledState = true
            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
        }
    }

    fun launchLocationPermission() {
        prefs.hasRequestedLocationPermission = true
        val perms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        locationPermissionLauncher.launch(perms)
    }

    // Handles GPS location requests, asking for permission if denied or directing to settings if permanently denied
    fun requestGpsWithPermissionPrompt() {
        isUserPinnedLocation = false
        pinnedLat = null
        pinnedLng = null

        val hasLoc = LocationHelper.hasLocationPermission(context)
        isLocationGranted = hasLoc
        isLocationDenied = !hasLoc

        if (hasLoc) {
            showLocationPermissionDialog = false
            showLocationDisclosureDialog = false
            if (!LocationHelper.isLocationServiceEnabled(context)) {
                showTurnOnGpsDialog = true
                requestTurnOnGps()
            } else {
                detectLocationAndFetch(forceRecenter = true)
            }
        } else {
            isGpsRequestedByUser = true
            if (LocationHelper.isLocationPermanentlyDenied(activity, prefs)) {
                showLocationPermissionDialog = true
            } else {
                showLocationDisclosureDialog = true
            }
        }
    }

    // Handles Location permission click from Settings screen
    fun requestLocationPermissionFromSettings() {
        val hasLoc = LocationHelper.hasLocationPermission(context)
        isLocationGranted = hasLoc
        isLocationDenied = !hasLoc

        if (hasLoc) {
            showLocationAlreadyGrantedDialog = true
        } else {
            isGpsRequestedByUser = true
            if (LocationHelper.isLocationPermanentlyDenied(activity, prefs)) {
                showLocationPermissionDialog = true
            } else {
                showLocationDisclosureDialog = true
            }
        }
    }

    // Handles notification alert toggle with real permission verification
    fun requestNotificationPermissionOrToggle() {
        val hasNotif = LocationHelper.hasNotificationPermission(context)
        isNotificationGranted = hasNotif

        if (fuelAlertsEnabledState && hasNotif) {
            prefs.fuelAlertsEnabled = false
            fuelAlertsEnabledState = false
            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
            android.widget.Toast.makeText(context, "Fuel price alerts disabled", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            if (hasNotif) {
                prefs.fuelAlertsEnabled = true
                fuelAlertsEnabledState = true
                com.google.firebase.messaging.FirebaseMessaging.getInstance()
                    .subscribeToTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
                android.widget.Toast.makeText(context, "Fuel price alerts enabled", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                isNotificationRequestedByUser = true
                if (LocationHelper.isNotificationPermanentlyDenied(activity, prefs)) {
                    showNotificationPermissionRequiredDialog = true
                } else {
                    showNotificationDisclosureDialog = true
                }
            }
        }
    }

    // Lifecycle Observer: Automatically detects location when returning to app with GPS turned on
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                inAppUpdateManager?.onResumeCheck()

                val hasLoc = LocationHelper.hasLocationPermission(context)
                val hasNotif = LocationHelper.hasNotificationPermission(context)
                isLocationGranted = hasLoc
                isLocationDenied = !hasLoc
                isNotificationGranted = hasNotif

                if (!hasNotif && prefs.fuelAlertsEnabled) {
                    prefs.fuelAlertsEnabled = false
                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
                }
                fuelAlertsEnabledState = prefs.fuelAlertsEnabled && hasNotif

                if (hasLoc) {
                    showLocationPermissionDialog = false
                    if (LocationHelper.isLocationServiceEnabled(context)) {
                        if (showTurnOnGpsDialog) {
                            showTurnOnGpsDialog = false
                        }
                        if (userLat == null || userLng == null || isGpsRequestedByUser) {
                            isGpsRequestedByUser = false
                            detectLocationAndFetch(forceRecenter = true)
                        }
                    } else if (isGpsRequestedByUser) {
                        showTurnOnGpsDialog = true
                        requestTurnOnGps()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            mapDebounceJob?.cancel()
            inAppUpdateManager?.onDestroy()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initial Load - Default to current GPS location or ask to turn on GPS, check updates & reviews
    LaunchedEffect(Unit) {
        inAppUpdateManager?.checkAndStartUpdate()
        if (activity != null) {
            InAppReviewHelper.checkAndPromptReviewIfEligible(activity, prefs)
        }
        if (LocationHelper.hasNotificationPermission(context) && prefs.fuelAlertsEnabled) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
        }
        if (!showFirstLaunch) {
            if (LocationHelper.hasLocationPermission(context)) {
                if (!LocationHelper.isLocationServiceEnabled(context)) {
                    showTurnOnGpsDialog = true
                    requestTurnOnGps()
                    fetchStations()
                } else {
                    detectLocationAndFetch()
                }
            } else {
                fetchStations()
            }
        }
    }

    // Re-fetch when fuel, radius, or sort changes
    fun onFuelChanged(newFuel: FuelType) {
        selectedFuel = newFuel
        prefs.fuelType = newFuel
        stations = sortStations(stations, sortMode, newFuel)
        fetchStations(isRefresh = true)
    }

    fun onRadiusChanged(newRadius: Int) {
        searchRadius = newRadius
        prefs.searchRadius = newRadius
        fetchStations(isRefresh = true)
    }

    fun onSortChanged(newSort: SortMode) {
        sortMode = newSort
        prefs.sortMode = newSort
        stations = sortStations(stations, newSort, selectedFuel)
        fetchStations(isRefresh = true)
    }

    // First Launch Screen
    if (showFirstLaunch) {
        FirstLaunchLocationScreen(
            onRequestLocation = {
                launchLocationPermission()
            },
            onSearchManually = {
                prefs.hasPromptedLocation = true
                showFirstLaunch = false
                showSearchScreen = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !LocationHelper.hasNotificationPermission(context) &&
                    !prefs.hasRequestedNotificationPermission) {
                    showNotificationDisclosureDialog = true
                } else if (LocationHelper.hasNotificationPermission(context)) {
                    prefs.fuelAlertsEnabled = true
                    fuelAlertsEnabledState = true
                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                        .subscribeToTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
                }
            }
        )
        return
    }

    // Search Screen
    if (showSearchScreen) {
        BackHandler {
            showSearchScreen = false
        }
        val initialSearchQuery = if (isUserPinnedLocation) {
            if (locationTitle.startsWith("Pinned: ")) {
                locationTitle.removePrefix("Pinned: ").trim()
            } else if (locationTitle == "Pinned Location") {
                ""
            } else {
                locationTitle
            }
        } else {
            ""
        }

        SearchScreen(
            initialQuery = initialSearchQuery,
            recentSearches = recentSearches,
            onSelectLocation = { loc ->
                isUserPinnedLocation = true
                pinnedLat = loc.latitude
                pinnedLng = loc.longitude
                centerLat = loc.latitude
                centerLng = loc.longitude
                locationTitle = loc.name
                isOutsideUk = !loc.isUk
                recenterTrigger++
                prefs.addRecentSearch(loc)
                recentSearches = prefs.getRecentSearches()
                showSearchScreen = false
                if (loc.isUk) {
                    fetchStations()
                } else {
                    stations = emptyList()
                    minPrice = null
                    maxPrice = null
                    isOffline = false
                }
            },
            onUseCurrentLocation = {
                showSearchScreen = false
                requestGpsWithPermissionPrompt()
            },
            onClearLocation = {
                isUserPinnedLocation = false
                pinnedLat = null
                pinnedLng = null
                locationTitle = "Selected Area"
                fetchStations()
            },
            onClearRecentSearches = {
                prefs.clearRecentSearches()
                recentSearches = emptyList()
            },
            onBack = { showSearchScreen = false }
        )
        return
    }

    // Map gesture handler: debounced pan & zoom
    val onMapMoved: (Double, Double, Boolean) -> Unit = { newLat, newLng, isZoom ->
        // When a location is explicitly marked/searched, zooming in/out or panning MUST NOT move the marked point!
        if (!isUserPinnedLocation) {
            mapDebounceJob?.cancel()
            mapDebounceJob = scope.launch {
                delay(700)
                val movedMiles = UKGeocodingHelper.distanceMiles(centerLat, centerLng, newLat, newLng)
                if (!isZoom && movedMiles > 0.5) {
                    val inUk = UKGeocodingHelper.isInsideUk(newLat, newLng)
                    centerLat = newLat
                    centerLng = newLng
                    if (inUk) {
                        isOutsideUk = false
                        locationTitle = "Selected Area"
                        fetchStations()
                    } else {
                        isOutsideUk = true
                        locationTitle = "Outside UK"
                        stations = emptyList()
                        minPrice = null
                        maxPrice = null
                        isOffline = false
                    }
                }
            }
        }
    }

    // Main App Shell with Persistent Top Header Bar, Bottom Navigation Bar, and In-App Update Snackbars
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PetrolSurface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentTab == AppTab.MAP) {
                TopHeaderBar(
                    locationTitle = locationTitle,
                    isOutsideUk = isOutsideUk,
                    isUserPinnedLocation = isUserPinnedLocation,
                    onClearPinnedLocation = {
                        isUserPinnedLocation = false
                        pinnedLat = null
                        pinnedLng = null
                        locationTitle = "Selected Area"
                        fetchStations()
                    },
                    searchRadius = searchRadius,
                    onRadiusSelected = { r -> onRadiusChanged(r) },
                    selectedFuel = selectedFuel,
                    onFuelSelected = { fuel -> onFuelChanged(fuel) },
                    isRefreshing = isRefreshing,
                    onRefreshClick = { fetchStations(isRefresh = true) },
                    onSearchClick = { showSearchScreen = true },
                    isLocationDenied = isLocationDenied,
                    onRequestLocation = { requestGpsWithPermissionPrompt() },
                    isOffline = isOffline
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.MAP -> {
                    MainMapScreen(
                        locationTitle = locationTitle,
                        centerLat = centerLat,
                        centerLng = centerLng,
                        userLat = userLat,
                        userLng = userLng,
                        stations = stations,
                        selectedStation = selectedStation,
                        selectedFuel = selectedFuel,
                        searchRadius = searchRadius,
                        sortMode = sortMode,
                        isLoading = isLoading,
                        isRefreshing = isRefreshing,
                        isOffline = isOffline,
                        isLocationDenied = isLocationDenied,
                        isOutsideUk = isOutsideUk,
                        isUserPinnedLocation = isUserPinnedLocation,
                        pinnedLat = pinnedLat,
                        pinnedLng = pinnedLng,
                        recenterTrigger = recenterTrigger,
                        minPrice = minPrice,
                        maxPrice = maxPrice,
                        onStationClick = { st ->
                            selectedStation = st
                            centerLat = st.latitude
                            centerLng = st.longitude
                            recenterTrigger++
                            showStationDetails = true
                        },
                        onFuelSelected = { fuel -> onFuelChanged(fuel) },
                        onRadiusSelected = { r -> onRadiusChanged(r) },
                        onSearchClick = { showSearchScreen = true },
                        onClearPinnedLocation = {
                            isUserPinnedLocation = false
                            pinnedLat = null
                            pinnedLng = null
                            locationTitle = "Selected Area"
                            fetchStations()
                        },
                        onFilterClick = { showFilterSheet = true },
                        onSortClick = { showSortSheet = true },
                        onRefreshClick = { fetchStations(isRefresh = true) },
                        onRecenterClick = {
                            requestGpsWithPermissionPrompt()
                        },
                        onOpenDirections = { st ->
                            handleOpenDirections(st)
                        },
                        onJumpToUk = {
                            isUserPinnedLocation = false
                            pinnedLat = null
                            pinnedLng = null
                            centerLat = 51.5074
                            centerLng = -0.1278
                            locationTitle = "London Centre"
                            isOutsideUk = false
                            recenterTrigger++
                            fetchStations()
                        },
                        onMapMoved = onMapMoved,
                        onMapLongClick = { lat, lng ->
                            isUserPinnedLocation = true
                            pinnedLat = lat
                            pinnedLng = lng
                            centerLat = lat
                            centerLng = lng
                            val inUk = UKGeocodingHelper.isInsideUk(lat, lng)
                            if (inUk) {
                                isOutsideUk = false
                                val town = UKGeocodingHelper.findNearestTown(lat, lng)
                                locationTitle = if (town != null) "Pinned: $town" else "Pinned Location"
                                recenterTrigger++
                                android.widget.Toast.makeText(
                                    context,
                                    "Location pinned on map",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                fetchStations(isRefresh = true)
                            } else {
                                isOutsideUk = true
                                locationTitle = "Outside UK"
                                stations = emptyList()
                                minPrice = null
                                maxPrice = null
                                isOffline = false
                                recenterTrigger++
                                android.widget.Toast.makeText(
                                    context,
                                    "Pinned point is outside UK coverage",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }

                AppTab.SETTINGS -> {
                    SettingsScreen(
                        currentFuel = selectedFuel,
                        currentRadius = searchRadius,
                        currentSort = sortMode,
                        currentNavApp = navigationApp,
                        isLocationGranted = isLocationGranted,
                        fuelAlertsEnabled = fuelAlertsEnabledState,
                        lastUpdatedTime = lastUpdatedTime,
                        onFuelClick = { showFuelSheet = true },
                        onRadiusClick = { showFilterSheet = true },
                        onSortClick = { showSortSheet = true },
                        onNavAppChanged = { app ->
                            navigationApp = app
                            prefs.navigationApp = app
                        },
                        onToggleAlerts = { requestNotificationPermissionOrToggle() },
                        onLocationPermissionClick = { requestLocationPermissionFromSettings() },
                        onRefreshPrices = { fetchStations(isRefresh = true) },
                        onClearSavedData = {
                            prefs.clearSavedData()
                            recentSearches = emptyList()
                            stations = emptyList()
                            lastUpdatedTime = null
                        },
                        prefs = prefs
                    )
                }
            }
        }
    }

    // Back gesture / hardware back button handling
    BackHandler(enabled = showDirectionsDialog) {
        showDirectionsDialog = false
        stationForDirections = null
    }
    BackHandler(enabled = !showDirectionsDialog && showStationDetails) {
        showStationDetails = false
    }
    BackHandler(enabled = !showDirectionsDialog && !showStationDetails && showFuelSheet) {
        showFuelSheet = false
    }
    BackHandler(enabled = !showDirectionsDialog && !showStationDetails && !showFuelSheet && showFilterSheet) {
        showFilterSheet = false
    }
    BackHandler(enabled = !showDirectionsDialog && !showStationDetails && !showFuelSheet && !showFilterSheet && showSortSheet) {
        showSortSheet = false
    }
    BackHandler(enabled = !showDirectionsDialog && !showStationDetails && !showFuelSheet && !showFilterSheet && !showSortSheet && showTurnOnGpsDialog) {
        showTurnOnGpsDialog = false
    }
    BackHandler(enabled = !showDirectionsDialog && !showStationDetails && !showFuelSheet && !showFilterSheet && !showSortSheet && !showTurnOnGpsDialog && currentTab != AppTab.MAP) {
        currentTab = AppTab.MAP
    }

    // Fuel Bottom Sheet Dialog (Popup for Default Fuel in Settings)
    if (showFuelSheet) {
        FuelBottomSheet(
            currentFuel = selectedFuel,
            onFuelSelected = { newFuel ->
                onFuelChanged(newFuel)
                showFuelSheet = false
            },
            onDismiss = { showFuelSheet = false }
        )
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentRadius = searchRadius,
            onApply = { newRadius -> onRadiusChanged(newRadius) },
            onDismiss = { showFilterSheet = false }
        )
    }

    // Sort Bottom Sheet
    if (showSortSheet) {
        SortBottomSheet(
            currentSort = sortMode,
            onSortSelected = { newSort -> onSortChanged(newSort) },
            onDismiss = { showSortSheet = false }
        )
    }

    // Station Detail Dialog
    if (showStationDetails && selectedStation != null) {
        StationDetailDialog(
            station = selectedStation!!,
            currentFuel = selectedFuel,
            searchRadius = searchRadius,
            onDismiss = { showStationDetails = false },
            onGetDirections = { st ->
                handleOpenDirections(st)
            },
            onViewOnMap = { st ->
                centerLat = st.latitude
                centerLng = st.longitude
                showStationDetails = false
            }
        )
    }

    // Directions Dialog
    if (showDirectionsDialog && stationForDirections != null) {
        DirectionsDialog(
            station = stationForDirections!!,
            selectedNavApp = navigationApp,
            rememberChoiceInitial = if (prefs.hasConfiguredNavigationAppChoice) prefs.rememberNavigationAppChoice else true,
            onNavAppChanged = { app ->
                navigationApp = app
                prefs.navigationApp = app
            },
            onRememberChoiceChanged = { remember ->
                prefs.rememberNavigationAppChoice = remember
            },
            onDismiss = {
                showDirectionsDialog = false
                stationForDirections = null
            }
        )
    }

    // Turn On Device GPS Dialog
    if (showTurnOnGpsDialog) {
        TurnOnGpsDialog(
            onTurnOnGps = {
                requestTurnOnGps()
            },
            onSearchManually = {
                showTurnOnGpsDialog = false
                showSearchScreen = true
            },
            onDismiss = {
                showTurnOnGpsDialog = false
            }
        )
    }

    // In-App Location Permission Required Dialog
    // In-App Location Permission Required Dialog (Permanently Denied)
    if (showLocationPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showLocationPermissionDialog = false
                isGpsRequestedByUser = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = PetrolBrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Location Permission Required",
                    color = PetrolPrimaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Petrol Map requires location access to find petrol stations and compare fuel prices near your current location. Please allow location access in App Settings.",
                    color = PetrolSecondaryText,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationPermissionDialog = false
                        LocationHelper.openAppSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PetrolBrandGreen,
                        contentColor = PetrolBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLocationPermissionDialog = false
                        isGpsRequestedByUser = false
                    }
                ) {
                    Text("Cancel", color = PetrolSecondaryText)
                }
            },
            containerColor = PetrolSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // In-App Location Permission Already Granted Dialog (From Settings)
    if (showLocationAlreadyGrantedDialog) {
        AlertDialog(
            onDismissRequest = { showLocationAlreadyGrantedDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = PetrolBrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Location Permission Granted",
                    color = PetrolPrimaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Petrol Map has permission to access your location while using the app to show nearby fuel prices and calculate driving distances.\n\nYou can manage permissions anytime in App Settings.",
                    color = PetrolSecondaryText,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationAlreadyGrantedDialog = false
                        LocationHelper.openAppSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PetrolBrandGreen,
                        contentColor = PetrolBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("App Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLocationAlreadyGrantedDialog = false }
                ) {
                    Text("OK", color = PetrolSecondaryText)
                }
            },
            containerColor = PetrolSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Prominent Location Disclosure Dialog (Google Play Policy Compliance)
    if (showLocationDisclosureDialog) {
        AlertDialog(
            onDismissRequest = {
                showLocationDisclosureDialog = false
                isGpsRequestedByUser = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = PetrolBrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Location Access Disclosure",
                    color = PetrolPrimaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Petrol Map accesses device location data (approximate and precise) while the app is in use to find nearby petrol stations, calculate driving distances, and show live pump prices.\n\nYour location data is processed locally on your device in real time, is never stored on our servers, and is never tracked in the background.",
                    color = PetrolSecondaryText,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationDisclosureDialog = false
                        launchLocationPermission()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PetrolBrandGreen,
                        contentColor = PetrolBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Agree & Continue", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLocationDisclosureDialog = false
                        isGpsRequestedByUser = false
                    }
                ) {
                    Text("Not Now", color = PetrolSecondaryText)
                }
            },
            containerColor = PetrolSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Prominent Notification Permission Disclosure Dialog (Google Play Policy Compliance)
    if (showNotificationDisclosureDialog) {
        AlertDialog(
            onDismissRequest = {
                showNotificationDisclosureDialog = false
                isNotificationRequestedByUser = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = PetrolBrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Fuel Price Drop Alerts",
                    color = PetrolPrimaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Allow notifications to receive timely alerts when petrol and diesel prices drop in your area and get notified of cheap fuel near you.\n\nYou can turn off alerts at any time in Settings.",
                    color = PetrolSecondaryText,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationDisclosureDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            prefs.hasRequestedNotificationPermission = true
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            if (LocationHelper.hasNotificationPermission(context)) {
                                prefs.fuelAlertsEnabled = true
                                fuelAlertsEnabledState = true
                                isNotificationGranted = true
                                com.google.firebase.messaging.FirebaseMessaging.getInstance()
                                    .subscribeToTopic(com.zeetech.petrolmap.fuelpricesuk.services.PetrolFirebaseMessagingService.TOPIC_UK_FUEL_ALERTS)
                                android.widget.Toast.makeText(context, "Fuel price alerts enabled", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                showNotificationPermissionRequiredDialog = true
                            }
                            isNotificationRequestedByUser = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PetrolBrandGreen,
                        contentColor = PetrolBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Enable Alerts", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNotificationDisclosureDialog = false
                        isNotificationRequestedByUser = false
                    }
                ) {
                    Text("Not Now", color = PetrolSecondaryText)
                }
            },
            containerColor = PetrolSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // In-App Notification Permission Required Dialog (Permanently Denied or OS Disabled)
    if (showNotificationPermissionRequiredDialog) {
        AlertDialog(
            onDismissRequest = {
                showNotificationPermissionRequiredDialog = false
                isNotificationRequestedByUser = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = PetrolBrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Notification Permission Required",
                    color = PetrolPrimaryText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Notifications are disabled for Petrol Map in device settings. Please enable notifications in App Settings to receive timely alerts when fuel prices drop.",
                    color = PetrolSecondaryText,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationPermissionRequiredDialog = false
                        isNotificationRequestedByUser = false
                        LocationHelper.openAppNotificationSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PetrolBrandGreen,
                        contentColor = PetrolBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNotificationPermissionRequiredDialog = false
                        isNotificationRequestedByUser = false
                    }
                ) {
                    Text("Cancel", color = PetrolSecondaryText)
                }
            },
            containerColor = PetrolSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }
}