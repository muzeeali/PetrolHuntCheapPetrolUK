package com.zeetech.petrolmap.fuelpricesuk.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import kotlin.math.abs

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    centerLat: Double,
    centerLng: Double,
    zoomLevel: Double = 13.5,
    recenterTrigger: Int = 0,
    stations: List<Station>,
    selectedStation: Station?,
    userLat: Double?,
    userLng: Double?,
    isPinnedLocation: Boolean = false,
    pinnedLat: Double? = null,
    pinnedLng: Double? = null,
    pinnedTitle: String? = null,
    onStationClick: (Station) -> Unit,
    onMapMoved: ((newLat: Double, newLng: Double, isZoom: Boolean) -> Unit)? = null,
    onMapLongClick: ((newLat: Double, newLng: Double) -> Unit)? = null,
    onMapReady: ((MapView) -> Unit)? = null
) {
    val context = LocalContext.current
    val lastHandledTrigger = remember { intArrayOf(recenterTrigger) }

    var isUserInteracting by remember { mutableStateOf(false) }

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            controller.setZoom(zoomLevel)
            controller.setCenter(GeoPoint(centerLat, centerLng))

            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_MOVE -> isUserInteracting = true
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> isUserInteracting = false
                }
                false
            }

            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    val center = mapCenter
                    if (center != null && isUserInteracting) {
                        onMapMoved?.invoke(center.latitude, center.longitude, false)
                    }
                    return true
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    val center = mapCenter
                    if (center != null && isUserInteracting) {
                        onMapMoved?.invoke(center.latitude, center.longitude, true)
                    }
                    return true
                }
            })

            onMapReady?.invoke(this)
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { map ->
            if (recenterTrigger != lastHandledTrigger[0]) {
                lastHandledTrigger[0] = recenterTrigger
                map.controller.animateTo(GeoPoint(centerLat, centerLng))
                if (map.zoomLevelDouble < 13.0) {
                    map.controller.setZoom(14.0)
                }
            }

            // Clear old markers
            map.overlays.clear()

            // 0. Long Press Event Listener Overlay
            if (onMapLongClick != null) {
                val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: org.osmdroid.util.GeoPoint?): Boolean {
                        return false
                    }

                    override fun longPressHelper(p: org.osmdroid.util.GeoPoint?): Boolean {
                        if (p != null) {
                            onMapLongClick.invoke(p.latitude, p.longitude)
                            return true
                        }
                        return false
                    }
                })
                map.overlays.add(eventsOverlay)
            }

            // 1. User location marker
            if (userLat != null && userLng != null) {
                val userMarker = Marker(map).apply {
                    position = GeoPoint(userLat, userLng)
                    title = "My Location"
                    icon = createUserLocationIcon(context)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
                map.overlays.add(userMarker)
            }

            // 2. Pinned / Searched Location Marker
            if (isPinnedLocation && pinnedLat != null && pinnedLng != null) {
                val pinMarker = Marker(map).apply {
                    position = GeoPoint(pinnedLat, pinnedLng)
                    title = pinnedTitle ?: "Pinned Location"
                    icon = createPinnedLocationIcon(context)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(pinMarker)
            }

            // 2. Station Price Markers
            val cheapestPrice = stations.minOfOrNull { it.price } ?: 0.0
            val highestPrice = stations.maxOfOrNull { it.price } ?: 0.0

            for (station in stations) {
                val isSelected = selectedStation?.id == station.id
                val isCheapest = station.price <= cheapestPrice + 0.1

                val markerColor = when {
                    isCheapest -> 0xFF35E879.toInt() // Green
                    station.price >= highestPrice - 1.0 -> 0xFFFF5C5C.toInt() // Red
                    else -> 0xFFFFC857.toInt() // Amber
                }

                val marker = Marker(map).apply {
                    position = GeoPoint(station.latitude, station.longitude)
                    title = station.name
                    subDescription = "${station.price}p"
                    icon = createPriceMarkerIcon(
                        context = context,
                        priceText = String.format("%.1fp", station.price),
                        color = markerColor,
                        isSelected = isSelected,
                        isCheapest = isCheapest
                    )
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onStationClick(station)
                        true
                    }
                }
                map.overlays.add(marker)
            }

            map.invalidate()
        }
    )
}

private fun createPriceMarkerIcon(
    context: Context,
    priceText: String,
    color: Int,
    isSelected: Boolean,
    isCheapest: Boolean
): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val width = (if (isSelected) 84 * density else 72 * density).toInt()
    val height = (if (isSelected) 40 * density else 34 * density).toInt()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val radius = height / 2f
    val rect = RectF(2f * density, 2f * density, width - 2f * density, height - 2f * density)
    canvas.drawRoundRect(rect, radius, radius, bgPaint)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = if (isSelected) 0xFFFFFFFF.toInt() else 0xFF0F1115.toInt()
        style = Paint.Style.STROKE
        strokeWidth = (if (isSelected) 2.5f else 1.5f) * density
    }
    canvas.drawRoundRect(rect, radius, radius, borderPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = 0xFF0F1115.toInt()
        textSize = (if (isSelected) 14f else 12.5f) * density
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    val yPos = (height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f)
    canvas.drawText(priceText, width / 2f, yPos, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun createUserLocationIcon(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val size = (26 * density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x5535E879.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, outerPaint)

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF35E879.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, 7f * density, innerPaint)

    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, 3.5f * density, corePaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun createPinnedLocationIcon(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val width = (32 * density).toInt()
    val height = (44 * density).toInt()

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 1. Subtle drop shadow beneath the pin needle tip
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x44000000.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawOval(
        RectF(width / 2f - 7f * density, height - 6f * density, width / 2f + 7f * density, height - 1f * density),
        shadowPaint
    )

    // 2. Draw Pin Teardrop Shape
    val pinPath = android.graphics.Path()
    val headRadius = 13f * density
    val headCenterY = 14f * density
    val headCenterX = width / 2f
    val tipX = width / 2f
    val tipY = height - 5f * density

    pinPath.moveTo(tipX, tipY)
    pinPath.quadTo(
        tipX - headRadius * 1.05f, headCenterY + headRadius * 0.7f,
        headCenterX - headRadius, headCenterY
    )
    pinPath.arcTo(
        RectF(headCenterX - headRadius, headCenterY - headRadius, headCenterX + headRadius, headCenterY + headRadius),
        180f,
        180f,
        false
    )
    pinPath.quadTo(
        tipX + headRadius * 1.05f, headCenterY + headRadius * 0.7f,
        tipX, tipY
    )
    pinPath.close()

    // Vibrant red/crimson fill
    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFEF4444.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawPath(pinPath, pinPaint)

    // Crisp white border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    canvas.drawPath(pinPath, borderPaint)

    // Inner White Dot
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(headCenterX, headCenterY, 4.5f * density, dotPaint)

    return BitmapDrawable(context.resources, bitmap)
}
