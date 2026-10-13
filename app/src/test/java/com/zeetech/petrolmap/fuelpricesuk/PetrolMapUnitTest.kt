package com.zeetech.petrolmap.fuelpricesuk

import com.zeetech.petrolmap.fuelpricesuk.data.local.UKGeocodingHelper
import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.data.model.NavigationApp
import com.zeetech.petrolmap.fuelpricesuk.data.model.SortMode
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import com.zeetech.petrolmap.fuelpricesuk.data.model.StationAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PetrolMapUnitTest {

    @Test
    fun testFuelTypeCodes() {
        assertEquals("E10", FuelType.E10.code)
        assertEquals("E5", FuelType.E5.code)
        assertEquals("B7", FuelType.DIESEL.code)
        assertEquals("SDV", FuelType.SDV.code)
    }

    @Test
    fun testUkBoundaryDetection() {
        // UK Locations
        assertTrue(UKGeocodingHelper.isInsideUk(51.5074, -0.1278)) // London
        assertTrue(UKGeocodingHelper.isInsideUk(55.9533, -3.1883)) // Edinburgh
        assertTrue(UKGeocodingHelper.isInsideUk(54.5973, -5.9301)) // Belfast
        assertTrue(UKGeocodingHelper.isInsideUk(51.4816, -3.1791)) // Cardiff

        // Non-UK Worldwide Locations
        assertFalse(UKGeocodingHelper.isInsideUk(48.8566, 2.3522)) // Paris
        assertFalse(UKGeocodingHelper.isInsideUk(40.7128, -74.0060)) // New York
        assertFalse(UKGeocodingHelper.isInsideUk(35.6762, 139.6503)) // Tokyo
        assertFalse(UKGeocodingHelper.isInsideUk(25.2048, 55.2708)) // Dubai
    }

    @Test
    fun testMultiFuelStationPrices() {
        val prices = mapOf(
            "E10" to 142.9,
            "E5" to 155.9,
            "B7" to 149.9,
            "SDV" to 169.9
        )
        val st = Station(
            id = "test-1",
            name = "Shell Forecourt",
            brand = "SHELL",
            latitude = 51.5,
            longitude = -0.1,
            price = 142.9,
            fuel = "E10",
            distanceMiles = 1.0,
            updatedAt = "2026-10-12T12:00:00Z",
            address = StationAddress("Main Rd", "W1A 1AA", "London"),
            prices = prices
        )
        assertEquals(142.9, st.prices["E10"])
        assertEquals(155.9, st.prices["E5"])
        assertEquals(149.9, st.prices["B7"])
        assertEquals(169.9, st.prices["SDV"])
    }

    @Test
    fun testStationSortingCheapest() {
        val s1 = createStation("1", 145.9, 1.2)
        val s2 = createStation("2", 139.9, 3.5)
        val s3 = createStation("3", 142.5, 0.8)

        val list = listOf(s1, s2, s3).sortedWith(compareBy({ it.price }, { it.distanceMiles }))
        assertEquals("2", list[0].id)
        assertEquals(139.9, list[0].price, 0.001)
        assertEquals("3", list[1].id)
        assertEquals("1", list[2].id)
    }

    @Test
    fun testStationSortingNearest() {
        val s1 = createStation("1", 145.9, 1.2)
        val s2 = createStation("2", 139.9, 3.5)
        val s3 = createStation("3", 142.5, 0.8)

        val list = listOf(s1, s2, s3).sortedWith(compareBy({ it.distanceMiles }, { it.price }))
        assertEquals("3", list[0].id)
        assertEquals(0.8, list[0].distanceMiles, 0.001)
        assertEquals("1", list[1].id)
        assertEquals("2", list[2].id)
    }

    @Test
    fun testHaversineFormulaAccuracy() {
        // Distance between Westminster (51.4998, -0.1246) and Clapham (51.4646, -0.1656) in miles
        val lat1 = 51.4998
        val lon1 = -0.1246
        val lat2 = 51.4646
        val lon2 = -0.1656

        val r = 3958.8 // miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c

        // Expected approx 3.0 miles
        assertTrue(distance > 2.5 && distance < 3.5)
    }

    @Test
    fun testNavigationAppPackages() {
        assertEquals("com.google.android.apps.maps", NavigationApp.GOOGLE_MAPS.packageName)
        assertEquals("com.waze", NavigationApp.WAZE.packageName)
        assertEquals(null, NavigationApp.SYSTEM_DEFAULT.packageName)
    }

    private fun createStation(id: String, price: Double, distance: Double): Station {
        return Station(
            id = id,
            name = "Test Station $id",
            brand = "BP",
            latitude = 51.5074,
            longitude = -0.1278,
            price = price,
            fuel = "E10",
            distanceMiles = distance,
            updatedAt = "2026-10-12T12:00:00Z",
            address = StationAddress("High Street", "SW1A 1AA", "London")
        )
    }
}
