package com.zeetech.petrolmap.fuelpricesuk.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.R
import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.data.model.SortMode
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import com.zeetech.petrolmap.fuelpricesuk.ui.components.FuelFinderDisclaimer
import com.zeetech.petrolmap.fuelpricesuk.ui.components.OsmMapView
import com.zeetech.petrolmap.fuelpricesuk.ui.components.StationCard
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPriceCheap
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPriceExpensive
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurface
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated

@Composable
fun MainMapScreen(
    locationTitle: String,
    centerLat: Double,
    centerLng: Double,
    userLat: Double?,
    userLng: Double?,
    stations: List<Station>,
    selectedStation: Station?,
    selectedFuel: FuelType,
    searchRadius: Int,
    sortMode: SortMode,
    isLoading: Boolean,
    isRefreshing: Boolean,
    isOffline: Boolean,
    isLocationDenied: Boolean,
    isOutsideUk: Boolean = false,
    isUserPinnedLocation: Boolean = false,
    pinnedLat: Double? = null,
    pinnedLng: Double? = null,
    recenterTrigger: Int = 0,
    minPrice: Double?,
    maxPrice: Double?,
    onStationClick: (Station) -> Unit,
    onFuelSelected: (FuelType) -> Unit,
    onRadiusSelected: (Int) -> Unit = {},
    onSearchClick: () -> Unit,
    onClearPinnedLocation: (() -> Unit)? = null,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onRecenterClick: () -> Unit,
    onOpenDirections: (Station) -> Unit,
    onJumpToUk: (() -> Unit)? = null,
    onMapMoved: ((newLat: Double, newLng: Double, isZoom: Boolean) -> Unit)? = null,
    onMapLongClick: ((newLat: Double, newLng: Double) -> Unit)? = null
) {
    var isSheetExpanded by remember { mutableStateOf(false) }

    // Dynamic minimum and maximum prices computed strictly from active selected stations
    val selectedResultPrices = remember(stations) {
        stations.map { it.price }.filter { it > 0.0 }
    }
    val effectiveMinPrice = selectedResultPrices.minOrNull() ?: minPrice
    val effectiveMaxPrice = selectedResultPrices.maxOrNull() ?: maxPrice

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(PetrolBackground)
    ) {
        val availableHeight = maxHeight
        val collapsedSheetHeight = 78.dp
        val expandedSheetHeight = (availableHeight - 64.dp).coerceAtLeast(200.dp)
        val sheetHeight by animateDpAsState(
            targetValue = if (isSheetExpanded) expandedSheetHeight else collapsedSheetHeight,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "sheetHeight"
        )
        val floatingControlsOffset by animateDpAsState(
            targetValue = if (isSheetExpanded) (sheetHeight + 10.dp) else 88.dp,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "floatingControlsOffset"
        )
            // (a) OpenStreetMap View (Starts below top bars buttons row)
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                centerLat = centerLat,
                centerLng = centerLng,
                recenterTrigger = recenterTrigger,
                stations = stations,
                selectedStation = selectedStation,
                userLat = userLat,
                userLng = userLng,
                isPinnedLocation = isUserPinnedLocation,
                pinnedLat = if (isUserPinnedLocation) (pinnedLat ?: centerLat) else null,
                pinnedLng = if (isUserPinnedLocation) (pinnedLng ?: centerLng) else null,
                pinnedTitle = locationTitle,
                onStationClick = { station ->
                    onStationClick(station)
                },
                onMapMoved = onMapMoved,
                onMapLongClick = onMapLongClick
            )

            // (b) Live Prices Loading Indicator Pill on Map
            if (isLoading || isRefreshing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PetrolSurfaceElevated.copy(alpha = 0.95f))
                        .border(1.dp, PetrolBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    color = PetrolBrandGreen,
                    strokeWidth = 2.dp
                )
                Text(
                    text = if (isRefreshing) "Updating prices..." else "Finding live prices...",
                    color = PetrolPrimaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // 4. Floating Map Controls (Low/High Price Range Badge on map left corner + Recenter GPS Button)
    Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = floatingControlsOffset, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Price range badge on map left corner
            if (!isOutsideUk && effectiveMinPrice != null && effectiveMaxPrice != null && effectiveMinPrice > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(PetrolSurfaceElevated.copy(alpha = 0.94f))
                        .border(1.dp, PetrolBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(PetrolPriceCheap)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Low ${String.format(java.util.Locale.US, "%.1f", effectiveMinPrice)}p",
                            color = PetrolPrimaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(7.dp))

                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(PetrolPriceExpensive)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "High ${String.format(java.util.Locale.US, "%.1f", effectiveMaxPrice)}p",
                            color = PetrolSecondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Recenter GPS button
            IconButton(
                onClick = onRecenterClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PetrolSurfaceElevated)
                    .border(1.dp, PetrolBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter GPS",
                    tint = PetrolBrandGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 4. Swipeable Bottom Stations Sheet (Reduced padding to fit more stations)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeight)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(PetrolSurface)
                .border(1.dp, PetrolBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -15) {
                            isSheetExpanded = true
                        } else if (dragAmount > 15) {
                            isSheetExpanded = false
                        }
                    }
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Drag handle pill at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isSheetExpanded = !isSheetExpanded }
                    .padding(vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PetrolBorder)
                )
            }

            // --- Collapsed State Header (Single prominent button to expand) ---
            if (!isSheetExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isOutsideUk) PetrolPriceExpensive else PetrolBrandGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isOutsideUk) "Outside UK Coverage" else "Cheapest nearby",
                                color = PetrolPrimaryText,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = if (isOutsideUk) {
                                "Pan into UK to see live prices"
                            } else if (isLoading && stations.isEmpty()) {
                                "Locating live fuel prices..."
                            } else if (effectiveMinPrice != null && effectiveMinPrice > 0) {
                                "from ${String.format(java.util.Locale.US, "%.1f", effectiveMinPrice)}p · ${stations.size} stations within ${searchRadius}mi"
                            } else {
                                "${stations.size} stations within ${searchRadius}mi"
                            },
                            color = PetrolSecondaryText,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // THE ONE BUTTON TO SWIPE / EXPAND LISTING
                    Button(
                        onClick = { isSheetExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PetrolBrandGreen,
                            contentColor = PetrolBackground
                        ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "${stations.size} Stations",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expand listing",
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            } else {
                // --- Expanded State Header (Relocated App Logo & Name + Single Collapse Button) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Relocated App Name & Logo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Petrol Map",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Petrol Map",
                            color = PetrolPrimaryText,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PetrolSurfaceElevated)
                                .border(1.dp, PetrolBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${stations.size} within ${searchRadius}mi",
                                color = PetrolBrandGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // THE ONE BUTTON TO COLLAPSE TO MAP
                    Button(
                        onClick = { isSheetExpanded = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PetrolSurfaceElevated,
                            contentColor = PetrolPrimaryText
                        ),
                        border = BorderStroke(1.dp, PetrolBorder),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "Map",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse to map",
                            tint = PetrolBrandGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Sub-bar in expanded view: Sort selection chip
                if (!isOutsideUk && stations.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (sortMode) {
                                SortMode.CHEAPEST -> "Ranked by cheapest fuel"
                                SortMode.NEAREST -> "Ranked by distance"
                                SortMode.LATEST -> "Ranked by latest updated"
                            },
                            color = PetrolSecondaryText,
                            fontSize = 11.5.sp
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(PetrolSurfaceElevated)
                                .border(1.dp, PetrolBorder, RoundedCornerShape(10.dp))
                                .clickable { onSortClick() }
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Sort",
                                tint = PetrolBrandGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = sortMode.label.uppercase(),
                                color = PetrolPrimaryText,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Scrollable Stations List with tight spacing to show more items
                if (isLoading && stations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = PetrolBrandGreen,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Finding live prices...",
                                color = PetrolSecondaryText,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (stations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOutsideUk) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = "Outside UK Pricing Coverage",
                                    color = PetrolPrimaryText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Fuel prices are only tracked across the United Kingdom. Pan or search inside the UK.",
                                    color = PetrolSecondaryText,
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row {
                                    Button(
                                        onClick = { onJumpToUk?.invoke() ?: onRecenterClick() },
                                        colors = ButtonDefaults.buttonColors(containerColor = PetrolBrandGreen, contentColor = PetrolBackground),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Jump to UK", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = onSearchClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = PetrolSurfaceElevated, contentColor = PetrolPrimaryText),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Search UK towns", fontSize = 11.5.sp)
                                    }
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No stations within ${searchRadius} miles",
                                    color = PetrolPrimaryText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Try increasing the distance radius or searching another area.",
                                    color = PetrolSecondaryText,
                                    fontSize = 11.5.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onRadiusSelected(searchRadius + 5) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PetrolBrandGreen, contentColor = PetrolBackground),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Increase radius to ${searchRadius + 5}mi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    val cheapestPrice = stations.minOfOrNull { it.price } ?: 0.0

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(stations) { station ->
                            StationCard(
                                station = station,
                                isCheapest = station.price <= cheapestPrice + 0.1,
                                isSelected = selectedStation?.id == station.id,
                                onClick = { onStationClick(station) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            FuelFinderDisclaimer(
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


