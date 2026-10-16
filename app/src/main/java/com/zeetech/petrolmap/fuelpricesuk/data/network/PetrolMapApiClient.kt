package com.zeetech.petrolmap.fuelpricesuk.data.network

import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.data.model.SortMode
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import com.zeetech.petrolmap.fuelpricesuk.data.model.StationAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LiveForecourtDetails(
    val slug: String,
    val prices: Map<String, Double>,
    val fuelUpdatedDates: Map<String, String>,
    val latestUpdated: String
)

sealed class StationsResult {
    data class Success(
        val stations: List<Station>,
        val isCached: Boolean,
        val isUk: Boolean,
        val attribution: String,
        val fetchedAt: String,
        val minPrice: Double?,
        val maxPrice: Double?,
        val avgPrice: Double?
    ) : StationsResult()

    data class Error(val message: String, val isNetworkError: Boolean) : StationsResult()
}

object PetrolMapApiClient {
    // High-Efficiency Edge Proxy Gateway (Cloudflare Workers)
    private const val WORKER_URL = "https://petrol-hunt-api-production.petrol-hunt.workers.dev"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun getNearbyStations(
        latitude: Double,
        longitude: Double,
        fuel: FuelType,
        radiusMiles: Int,
        sort: SortMode,
        forceRefresh: Boolean = false
    ): StationsResult = withContext(Dispatchers.IO) {

        // Check UK coordinates: latitude 49.5 to 61.5, longitude -9.5 to 2.5
        val inUk = latitude in 49.5..61.5 && longitude in -9.5..2.5
        if (!inUk) {
            return@withContext StationsResult.Success(
                stations = emptyList(),
                isCached = false,
                isUk = false,
                attribution = "Prices sourced via Fuel Finder & CMA Open Data Scheme",
                fetchedAt = "",
                minPrice = null,
                maxPrice = null,
                avgPrice = null
            )
        }

        val refreshParam = if (forceRefresh) "&refresh=true" else ""
        val cacheBuster = if (forceRefresh) "&_t=${System.currentTimeMillis()}" else ""
        val workerUrl = "$WORKER_URL/v1/stations?lat=$latitude&lng=$longitude&fuel=${fuel.code}&radius=$radiusMiles&sort=${sort.code}$refreshParam$cacheBuster"

        val request = Request.Builder()
            .url(workerUrl)
            .header("User-Agent", "PetrolMapUK/${com.zeetech.petrolmap.fuelpricesuk.BuildConfig.VERSION_NAME} (Android Mobile App; en-GB)")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                val isEdgeCached = response.header("X-Edge-Cache") == "HIT"

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    if (json.optBoolean("success", false)) {
                        val isUkResp = json.optBoolean("isUk", true)
                        if (!isUkResp) {
                            return@withContext StationsResult.Success(
                                stations = emptyList(),
                                isCached = isEdgeCached,
                                isUk = false,
                                attribution = json.optString("attribution", "Prices sourced via Fuel Finder & CMA Open Data Scheme"),
                                fetchedAt = json.optString("fetchedAt", ""),
                                minPrice = null,
                                maxPrice = null,
                                avgPrice = null
                            )
                        }

                        val stations = parseStationsArray(json.optJSONArray("stations"), fuel)
                        val areaStats = json.optJSONObject("areaStats")
                        val avgPrice = areaStats?.optDouble("avg")?.takeIf { !it.isNaN() }
                        val minPrice = areaStats?.optDouble("min")?.takeIf { !it.isNaN() } ?: stations.minOfOrNull { it.price }
                        val maxPrice = areaStats?.optDouble("max")?.takeIf { !it.isNaN() } ?: stations.maxOfOrNull { it.price }

                        return@withContext StationsResult.Success(
                            stations = stations,
                            isCached = isEdgeCached,
                            isUk = true,
                            attribution = json.optString("attribution", "Prices sourced via Fuel Finder & CMA Open Data Scheme"),
                            fetchedAt = json.optString("fetchedAt", ""),
                            minPrice = minPrice,
                            maxPrice = maxPrice,
                            avgPrice = avgPrice
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Worker network exception
        }

        StationsResult.Error(
            message = "Couldn't update fuel prices. Check your connection.",
            isNetworkError = true
        )
    }

    suspend fun fetchStationLiveDetails(slug: String): LiveForecourtDetails? = withContext(Dispatchers.IO) {
        if (slug.isBlank()) return@withContext null

        try {
            val workerUrl = "$WORKER_URL/v1/station?slug=${slug}"
            val req = Request.Builder()
                .url(workerUrl)
                .header("User-Agent", "PetrolMapUK/${com.zeetech.petrolmap.fuelpricesuk.BuildConfig.VERSION_NAME} (Android Mobile App; en-GB)")
                .header("Accept", "application/json")
                .build()
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string()
                if (resp.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    if (json.optBoolean("success", false)) {
                        val pricesMap = mutableMapOf<String, Double>()
                        val pricesObj = json.optJSONObject("prices")
                        if (pricesObj != null) {
                            val pKeys = pricesObj.keys()
                            while (pKeys.hasNext()) {
                                val pk = pKeys.next()
                                val pv = pricesObj.optDouble(pk)
                                if (!pv.isNaN() && pv > 0) pricesMap[pk] = pv
                            }
                        }

                        val datesMap = mutableMapOf<String, String>()
                        val datesObj = json.optJSONObject("updatedDates")
                        if (datesObj != null) {
                            val dKeys = datesObj.keys()
                            while (dKeys.hasNext()) {
                                val dk = dKeys.next()
                                val dv = datesObj.optString(dk)
                                if (dv.isNotBlank()) datesMap[dk] = dv
                            }
                        }

                        return@withContext LiveForecourtDetails(
                            slug = slug,
                            prices = pricesMap,
                            fuelUpdatedDates = datesMap,
                            latestUpdated = json.optString("latestUpdated", "")
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Error handling
        }

        null
    }

    private fun parseStationsArray(stationsArray: org.json.JSONArray?, selectedFuel: FuelType): List<Station> {
        val stations = mutableListOf<Station>()
        if (stationsArray == null) return stations

        for (i in 0 until stationsArray.length()) {
            val item = stationsArray.getJSONObject(i)
            val addrObj = item.optJSONObject("address")
            val addr = StationAddress(
                line1 = addrObj?.optString("line1") ?: "",
                postcode = addrObj?.optString("postcode") ?: "",
                town = addrObj?.optString("town") ?: "UK"
            )

            val pricesMap = mutableMapOf<String, Double>()
            val pricesObj = item.optJSONObject("prices")
            if (pricesObj != null) {
                val keys = pricesObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = pricesObj.optDouble(k)
                    if (!v.isNaN() && v > 0) {
                        pricesMap[k] = v
                    }
                }
            }
            if (!pricesMap.containsKey(selectedFuel.code)) {
                val p = item.optDouble("price")
                if (!p.isNaN() && p > 0) pricesMap[selectedFuel.code] = p
            }

            val updatedDatesMap = mutableMapOf<String, String>()
            val datesObj = item.optJSONObject("fuelUpdatedDates")
            if (datesObj != null) {
                val dKeys = datesObj.keys()
                while (dKeys.hasNext()) {
                    val dk = dKeys.next()
                    val dv = datesObj.optString(dk)
                    if (dv.isNotBlank()) updatedDatesMap[dk] = dv
                }
            }

            val slug = item.optString("slug", "")

            stations.add(
                Station(
                    id = item.optString("id", i.toString()),
                    name = item.optString("name", "Petrol Station"),
                    brand = item.optString("brand", "Independent"),
                    latitude = item.getDouble("latitude"),
                    longitude = item.getDouble("longitude"),
                    address = addr,
                    price = item.getDouble("price"),
                    fuel = item.optString("fuel", selectedFuel.code),
                    distanceMiles = item.optDouble("distanceMiles", 0.0),
                    updatedAt = item.optString("updatedAt", ""),
                    source = item.optString("source", "fuel_finder"),
                    diffFromAvg = item.optDouble("diffFromAvg", 0.0),
                    prices = pricesMap,
                    fuelUpdatedDates = updatedDatesMap,
                    slug = slug
                )
            )
        }
        return stations
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 3958.8 // miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun parseTimestampToEpoch(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        try {
            val cleaned = dateStr.replace("Last updated by Fuel Finder", "")
                .replace("Last updated by Retailer", "")
                .replace("Last updated by CMA", "")
                .replace("Last updated", "")
                .trim()

            if (cleaned.contains("T")) {
                val isoCleaned = if (cleaned.endsWith("Z")) cleaned.substring(0, cleaned.length - 1) else cleaned
                val format = if (isoCleaned.contains(".")) {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US)
                } else {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                }
                format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                return format.parse(isoCleaned)?.time ?: 0L
            }

            val regex = Regex("(\\d{1,2})/(\\d{1,2})/(\\d{2,4})(?:\\s+(\\d{1,2}):(\\d{2})(?::(\\d{2}))?)?")
            val match = regex.find(cleaned)
            if (match != null) {
                val (d, m, y) = match.destructured
                val day = d.toInt()
                val month = m.toInt() - 1
                val year = if (y.length == 2) 2000 + y.toInt() else y.toInt()
                val hour = match.groupValues.getOrNull(4)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
                val min = match.groupValues.getOrNull(5)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
                val sec = match.groupValues.getOrNull(6)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0

                val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                calendar.set(year, month, day, hour, min, sec)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                return calendar.timeInMillis
            }
        } catch (_: Exception) {}
        return 0L
    }
}
