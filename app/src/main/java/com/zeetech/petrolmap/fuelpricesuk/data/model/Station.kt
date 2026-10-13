package com.zeetech.petrolmap.fuelpricesuk.data.model

data class Station(
    val id: String,
    val name: String,
    val brand: String,
    val latitude: Double,
    val longitude: Double,
    val address: StationAddress,
    val price: Double,
    val fuel: String,
    val distanceMiles: Double,
    val updatedAt: String,
    val source: String = "fuel_finder",
    val diffFromAvg: Double = 0.0,
    val prices: Map<String, Double> = emptyMap(),
    val fuelUpdatedDates: Map<String, String> = emptyMap(),
    val slug: String = ""
)

data class StationAddress(
    val line1: String,
    val postcode: String,
    val town: String
)

enum class FuelType(val code: String, val label: String, val sublabel: String) {
    E10("E10", "E10 Petrol", "Unleaded"),
    E5("E5", "E5 Petrol", "Super"),
    DIESEL("B7", "Diesel", "B7"),
    SDV("SDV", "Prem. Diesel", "SDV")
}

enum class SortMode(val code: String, val label: String) {
    CHEAPEST("cheapest", "Cheapest"),
    NEAREST("nearest", "Nearest"),
    LATEST("latest", "Latest")
}

enum class NavigationApp(val label: String, val packageName: String?) {
    SYSTEM_DEFAULT("System default", null),
    GOOGLE_MAPS("Google Maps", "com.google.android.apps.maps"),
    WAZE("Waze", "com.waze")
}

data class SearchLocation(
    val name: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    val isUk: Boolean = true
)
