package com.zeetech.petrolmap.fuelpricesuk.data.local

import android.content.Context
import android.content.SharedPreferences
import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.data.model.NavigationApp
import com.zeetech.petrolmap.fuelpricesuk.data.model.SearchLocation
import com.zeetech.petrolmap.fuelpricesuk.data.model.SortMode
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import com.zeetech.petrolmap.fuelpricesuk.data.model.StationAddress
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("petrol_hunt_prefs", Context.MODE_PRIVATE)

    var fuelType: FuelType
        get() {
            val code = prefs.getString("fuel_type", FuelType.E10.name) ?: FuelType.E10.name
            return try { FuelType.valueOf(code) } catch (e: Exception) { FuelType.E10 }
        }
        set(value) = prefs.edit().putString("fuel_type", value.name).apply()

    var searchRadius: Int
        get() = prefs.getInt("search_radius", 5)
        set(value) = prefs.edit().putInt("search_radius", value).apply()

    var sortMode: SortMode
        get() {
            val code = prefs.getString("sort_mode", SortMode.CHEAPEST.name) ?: SortMode.CHEAPEST.name
            return try { SortMode.valueOf(code) } catch (e: Exception) { SortMode.CHEAPEST }
        }
        set(value) = prefs.edit().putString("sort_mode", value.name).apply()

    var navigationApp: NavigationApp
        get() {
            val name = prefs.getString("nav_app", NavigationApp.SYSTEM_DEFAULT.name) ?: NavigationApp.SYSTEM_DEFAULT.name
            return try { NavigationApp.valueOf(name) } catch (e: Exception) { NavigationApp.SYSTEM_DEFAULT }
        }
        set(value) = prefs.edit().putString("nav_app", value.name).apply()

    var rememberNavigationAppChoice: Boolean
        get() = prefs.getBoolean("remember_nav_app_choice", true)
        set(value) = prefs.edit().putBoolean("remember_nav_app_choice", value).apply()

    val hasConfiguredNavigationAppChoice: Boolean
        get() = prefs.contains("remember_nav_app_choice")

    var hasPromptedLocation: Boolean
        get() = prefs.getBoolean("has_prompted_location", false)
        set(value) = prefs.edit().putBoolean("has_prompted_location", value).apply()

    var hasRequestedLocationPermission: Boolean
        get() = prefs.getBoolean("has_requested_location_permission", false)
        set(value) = prefs.edit().putBoolean("has_requested_location_permission", value).apply()

    var hasRequestedNotificationPermission: Boolean
        get() = prefs.getBoolean("has_requested_notification_permission", false)
        set(value) = prefs.edit().putBoolean("has_requested_notification_permission", value).apply()

    var fuelAlertsEnabled: Boolean
        get() = prefs.getBoolean("fuel_alerts_enabled", true)
        set(value) = prefs.edit().putBoolean("fuel_alerts_enabled", value).apply()

    var firstLaunchTimestamp: Long
        get() {
            var ts = prefs.getLong("first_launch_ts", 0L)
            if (ts == 0L) {
                ts = System.currentTimeMillis()
                prefs.edit().putLong("first_launch_ts", ts).apply()
            }
            return ts
        }
        set(value) = prefs.edit().putLong("first_launch_ts", value).apply()

    var lastReviewPromptTimestamp: Long
        get() = prefs.getLong("last_review_prompt_ts", 0L)
        set(value) = prefs.edit().putLong("last_review_prompt_ts", value).apply()

    var reviewPromptCount: Int
        get() = prefs.getInt("review_prompt_count", 0)
        set(value) = prefs.edit().putInt("review_prompt_count", value).apply()

    var hasUserRatedApp: Boolean
        get() = prefs.getBoolean("has_user_rated_app", false)
        set(value) = prefs.edit().putBoolean("has_user_rated_app", value).apply()

    fun shouldPromptInAppReview(): Boolean {
        if (hasUserRatedApp) return false
        val now = System.currentTimeMillis()
        val timeSinceFirstLaunch = now - firstLaunchTimestamp
        val oneDayMillis = 24L * 60 * 60 * 1000L
        val oneWeekMillis = 7L * oneDayMillis
        val oneMonthMillis = 30L * oneDayMillis

        if (timeSinceFirstLaunch < oneDayMillis) return false

        val count = reviewPromptCount
        val timeSinceLastPrompt = now - lastReviewPromptTimestamp

        return when (count) {
            0 -> true
            1 -> timeSinceLastPrompt >= oneWeekMillis
            else -> timeSinceLastPrompt >= oneMonthMillis
        }
    }

    fun markReviewPrompted() {
        reviewPromptCount += 1
        lastReviewPromptTimestamp = System.currentTimeMillis()
    }

    fun markUserRated() {
        hasUserRatedApp = true
        lastReviewPromptTimestamp = System.currentTimeMillis()
    }

    var lastUpdatedTime: String?
        get() = prefs.getString("last_updated_time", null)
        set(value) = prefs.edit().putString("last_updated_time", value).apply()

    var lastUserLat: Double?
        get() {
            val v = prefs.getString("last_user_lat", null) ?: return null
            return v.toDoubleOrNull()
        }
        set(value) {
            if (value != null) {
                prefs.edit().putString("last_user_lat", value.toString()).apply()
            } else {
                prefs.edit().remove("last_user_lat").apply()
            }
        }

    var lastUserLng: Double?
        get() {
            val v = prefs.getString("last_user_lng", null) ?: return null
            return v.toDoubleOrNull()
        }
        set(value) {
            if (value != null) {
                prefs.edit().putString("last_user_lng", value.toString()).apply()
            } else {
                prefs.edit().remove("last_user_lng").apply()
            }
        }

    fun getRecentSearches(): List<SearchLocation> {
        val json = prefs.getString("recent_searches", null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<SearchLocation>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SearchLocation(
                        name = obj.getString("name"),
                        subtitle = obj.optString("subtitle", ""),
                        latitude = obj.getDouble("lat"),
                        longitude = obj.getDouble("lng"),
                        isUk = obj.optBoolean("isUk", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addRecentSearch(location: SearchLocation) {
        val current = getRecentSearches().toMutableList()
        current.removeAll { it.name.equals(location.name, ignoreCase = true) }
        current.add(0, location)
        val trimmed = if (current.size > 5) current.subList(0, 5) else current
        val array = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("subtitle", item.subtitle)
            obj.put("lat", item.latitude)
            obj.put("lng", item.longitude)
            obj.put("isUk", item.isUk)
            array.put(obj)
        }
        prefs.edit().putString("recent_searches", array.toString()).apply()
    }

    fun clearRecentSearches() {
        prefs.edit().remove("recent_searches").apply()
    }

    fun saveCachedStations(stations: List<Station>, updatedTime: String) {
        try {
            val array = JSONArray()
            for (st in stations) {
                val obj = JSONObject()
                obj.put("id", st.id)
                obj.put("name", st.name)
                obj.put("brand", st.brand)
                obj.put("lat", st.latitude)
                obj.put("lng", st.longitude)
                obj.put("price", st.price)
                obj.put("fuel", st.fuel)
                obj.put("dist", st.distanceMiles)
                obj.put("updatedAt", st.updatedAt)
                obj.put("line1", st.address.line1)
                obj.put("postcode", st.address.postcode)
                obj.put("town", st.address.town)
                obj.put("diffFromAvg", st.diffFromAvg)
                obj.put("slug", st.slug)

                val pricesObj = JSONObject()
                st.prices.forEach { (k, v) -> pricesObj.put(k, v) }
                obj.put("prices", pricesObj)

                val datesObj = JSONObject()
                st.fuelUpdatedDates.forEach { (k, v) -> datesObj.put(k, v) }
                obj.put("fuelUpdatedDates", datesObj)

                array.put(obj)
            }
            prefs.edit()
                .putString("cached_stations", array.toString())
                .putString("last_updated_time", updatedTime)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCachedStations(): List<Station> {
        val json = prefs.getString("cached_stations", null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<Station>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val pricesMap = mutableMapOf<String, Double>()
                val pricesObj = obj.optJSONObject("prices")
                if (pricesObj != null) {
                    val keys = pricesObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val pv = pricesObj.optDouble(k, Double.NaN)
                        if (!pv.isNaN() && pv > 0) {
                            pricesMap[k] = pv
                        }
                    }
                }

                val datesMap = mutableMapOf<String, String>()
                val datesObj = obj.optJSONObject("fuelUpdatedDates")
                if (datesObj != null) {
                    val dKeys = datesObj.keys()
                    while (dKeys.hasNext()) {
                        val dk = dKeys.next()
                        val dv = datesObj.optString(dk, "")
                        if (dv.isNotBlank()) datesMap[dk] = dv
                    }
                }

                val id = obj.optString("id", "")
                val lat = obj.optDouble("lat", 0.0)
                val lng = obj.optDouble("lng", 0.0)
                if (id.isBlank() || lat == 0.0 || lng == 0.0) continue

                list.add(
                    Station(
                        id = id,
                        name = obj.optString("name", "Petrol Station"),
                        brand = obj.optString("brand", "Independent"),
                        latitude = lat,
                        longitude = lng,
                        price = obj.optDouble("price", 0.0),
                        fuel = obj.optString("fuel", "E10"),
                        distanceMiles = obj.optDouble("dist", 0.0),
                        updatedAt = obj.optString("updatedAt", ""),
                        diffFromAvg = obj.optDouble("diffFromAvg", 0.0),
                        prices = pricesMap,
                        fuelUpdatedDates = datesMap,
                        slug = obj.optString("slug", ""),
                        address = StationAddress(
                            line1 = obj.optString("line1", ""),
                            postcode = obj.optString("postcode", ""),
                            town = obj.optString("town", "")
                        )
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearSavedData() {
        prefs.edit()
            .remove("cached_stations")
            .remove("last_updated_time")
            .remove("recent_searches")
            .apply()
    }
}
