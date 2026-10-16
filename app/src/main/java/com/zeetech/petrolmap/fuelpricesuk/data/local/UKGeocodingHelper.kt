package com.zeetech.petrolmap.fuelpricesuk.data.local

import com.zeetech.petrolmap.fuelpricesuk.data.model.SearchLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object UKGeocodingHelper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun isInsideUk(lat: Double, lng: Double): Boolean {
        // Approximate UK bounding box: latitude 49.5 to 61.5, longitude -9.5 to 2.5
        return lat in 49.5..61.5 && lng in -9.5..2.5
    }

    fun distanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 3958.8 // miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private val presetLocations = listOf(
        SearchLocation("London Centre", "Greater London, SW1A", 51.5074, -0.1278, true),
        SearchLocation("Westminster, London", "SW1A 1AA · Central London", 51.4998, -0.1246, true),
        SearchLocation("Clapham, London", "SW11 · South London", 51.4646, -0.1656, true),
        SearchLocation("Birmingham", "West Midlands · Central Hub", 52.4862, -1.8904, true),
        SearchLocation("Manchester", "Greater Manchester · City Centre", 53.4808, -2.2426, true),
        SearchLocation("Leeds", "West Yorkshire · LS1", 53.7997, -1.5492, true),
        SearchLocation("Glasgow", "Scotland · City Centre", 55.8642, -4.2518, true),
        SearchLocation("Liverpool", "Merseyside · Waterfront", 53.4084, -2.9916, true),
        SearchLocation("Bristol", "South West · Central Hub", 51.4545, -2.5879, true),
        SearchLocation("Sheffield", "South Yorkshire", 53.3811, -1.4701, true),
        SearchLocation("Edinburgh", "Scotland · Capital", 55.9533, -3.1883, true),
        SearchLocation("Newcastle upon Tyne", "Tyne and Wear", 54.9783, -1.6178, true),
        SearchLocation("Cardiff", "Wales · Capital", 51.4816, -3.1791, true),
        SearchLocation("Belfast", "Northern Ireland", 54.5973, -5.9301, true),
        SearchLocation("M1 Corridor (Watford / Luton)", "M1 Motorway Hub", 51.8787, -0.4200, true),
        SearchLocation("M25 Ring (Heathrow / Surrey)", "M25 Orbit Junction", 51.4700, -0.4543, true)
    )

    fun getPopularHubs(): List<SearchLocation> {
        return presetLocations.take(8)
    }

    fun findNearestTown(lat: Double, lng: Double): String? {
        val nearest = presetLocations.minByOrNull { distanceMiles(lat, lng, it.latitude, it.longitude) }
        return if (nearest != null && distanceMiles(lat, lng, nearest.latitude, nearest.longitude) < 15.0) {
            nearest.name
        } else {
            null
        }
    }

    suspend fun search(query: String): List<SearchLocation> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<SearchLocation>()

        // 1. Check local preset matches
        val matchingPresets = presetLocations.filter {
            it.name.contains(clean, ignoreCase = true) || it.subtitle.contains(clean, ignoreCase = true)
        }
        results.addAll(matchingPresets)

        // 2. If it matches a UK postcode format (postcodes.io)
        val cleanPostcode = clean.replace(" ", "")
        if (cleanPostcode.length in 2..8) {
            try {
                val url = "https://api.postcodes.io/postcodes/${cleanPostcode}"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "PetrolMapUK/${com.zeetech.petrolmap.fuelpricesuk.BuildConfig.VERSION_NAME}")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            if (json.optInt("status") == 200 && json.has("result")) {
                                val res = json.getJSONObject("result")
                                val post = res.optString("postcode", clean)
                                val adminDistrict = res.optString("admin_district", "UK")
                                val region = res.optString("region", "")
                                val lat = res.getDouble("latitude")
                                val lng = res.getDouble("longitude")

                                val loc = SearchLocation(
                                    name = post,
                                    subtitle = "$adminDistrict, $region",
                                    latitude = lat,
                                    longitude = lng,
                                    isUk = true
                                )
                                if (results.none { it.name.equals(post, ignoreCase = true) }) {
                                    results.add(0, loc)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore postcode lookup error and proceed to global search
            }
        }

        // 3. Global OpenStreetMap Nominatim Search (covers ANY place in the world)
        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=8"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "PetrolMapUK/${com.zeetech.petrolmap.fuelpricesuk.BuildConfig.VERSION_NAME}")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val array = JSONArray(body)
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val name = item.optString("name", item.optString("display_name").split(",").firstOrNull() ?: clean)
                            val displayName = item.optString("display_name", "")
                            val lat = item.getDouble("lat")
                            val lng = item.getDouble("lon")

                            val inUk = isInsideUk(lat, lng) || displayName.contains("United Kingdom", ignoreCase = true)
                            val subtitle = if (inUk) {
                                displayName.split(",").drop(1).take(2).joinToString(",").trim()
                            } else {
                                val parts = displayName.split(",")
                                val country = parts.lastOrNull()?.trim() ?: "International"
                                "$country · Outside UK"
                            }

                            val loc = SearchLocation(
                                name = name,
                                subtitle = subtitle,
                                latitude = lat,
                                longitude = lng,
                                isUk = inUk
                            )

                            if (results.none { it.name.equals(loc.name, ignoreCase = true) && Math.abs(it.latitude - loc.latitude) < 0.05 }) {
                                results.add(loc)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore network failure
        }

        results
    }
}
