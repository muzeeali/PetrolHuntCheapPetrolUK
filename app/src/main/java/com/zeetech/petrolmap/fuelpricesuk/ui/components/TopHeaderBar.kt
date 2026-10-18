package com.zeetech.petrolmap.fuelpricesuk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPriceExpensive
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurface
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated

/**
 * Top header bar component containing the location search input, radius filter,
 * fuel type selector, and quick refresh buttons.
 * Designed to be permanently docked at the top of the app in Scaffold.topBar.
 */
@Composable
fun TopHeaderBar(
    locationTitle: String,
    isOutsideUk: Boolean,
    isUserPinnedLocation: Boolean,
    onClearPinnedLocation: (() -> Unit)?,
    searchRadius: Int,
    onRadiusSelected: (Int) -> Unit,
    selectedFuel: FuelType,
    onFuelSelected: (FuelType) -> Unit,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onSearchClick: () -> Unit,
    isLocationDenied: Boolean,
    isOffline: Boolean,
    modifier: Modifier = Modifier
) {
    var isDistanceMenuOpen by remember { mutableStateOf(false) }
    var isFuelMenuOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PetrolSurface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // (a) Search Bar Input
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PetrolBackground)
                        .border(1.dp, PetrolBorder, RoundedCornerShape(10.dp))
                        .clickable { onSearchClick() }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PetrolBrandGreen,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOutsideUk) "Outside UK" else locationTitle.ifBlank { "Search UK area..." },
                        color = if (isOutsideUk) PetrolPriceExpensive else PetrolPrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUserPinnedLocation && onClearPinnedLocation != null) {
                        IconButton(
                            onClick = onClearPinnedLocation,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Pin",
                                tint = PetrolSecondaryText,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // (b) Distance Filter Button with Popup Menu
                Box {
                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PetrolBackground)
                            .border(1.dp, PetrolBorder, RoundedCornerShape(10.dp))
                            .clickable { isDistanceMenuOpen = true }
                            .padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${searchRadius} mi",
                            color = PetrolPrimaryText,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Distance",
                            tint = PetrolSecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isDistanceMenuOpen,
                        onDismissRequest = { isDistanceMenuOpen = false },
                        modifier = Modifier
                            .background(PetrolSurfaceElevated)
                            .border(1.dp, PetrolBorder, RoundedCornerShape(10.dp))
                    ) {
                        listOf(2, 5, 10, 15, 20).forEach { radius ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "$radius miles",
                                            color = if (searchRadius == radius) PetrolBrandGreen else PetrolPrimaryText,
                                            fontWeight = if (searchRadius == radius) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.5.sp
                                        )
                                        if (searchRadius == radius) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = PetrolBrandGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    isDistanceMenuOpen = false
                                    onRadiusSelected(radius)
                                }
                            )
                        }
                    }
                }

                // (c) Fuel Type Button with Popup Menu
                Box {
                    val fuelDot = when (selectedFuel) {
                        FuelType.E10, FuelType.E5 -> PetrolBrandGreen
                        FuelType.DIESEL -> Color(0xFFFFB300)
                        FuelType.SDV -> Color(0xFFFF7043)
                    }

                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PetrolBackground)
                            .border(1.dp, PetrolBorder, RoundedCornerShape(10.dp))
                            .clickable { isFuelMenuOpen = true }
                            .padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(fuelDot)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = selectedFuel.code,
                            color = PetrolPrimaryText,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Fuel Type",
                            tint = PetrolSecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isFuelMenuOpen,
                        onDismissRequest = { isFuelMenuOpen = false },
                        modifier = Modifier
                            .background(PetrolSurfaceElevated)
                            .border(1.dp, PetrolBorder, RoundedCornerShape(10.dp))
                    ) {
                        FuelType.entries.forEach { fuel ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(
                                                text = "${fuel.code} · ${fuel.label}",
                                                color = if (selectedFuel == fuel) PetrolBrandGreen else PetrolPrimaryText,
                                                fontWeight = if (selectedFuel == fuel) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.5.sp
                                            )
                                            Text(
                                                text = fuel.sublabel,
                                                color = PetrolSecondaryText,
                                                fontSize = 11.sp
                                            )
                                        }
                                        if (selectedFuel == fuel) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = PetrolBrandGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    isFuelMenuOpen = false
                                    onFuelSelected(fuel)
                                }
                            )
                        }
                    }
                }

                // (d) Quick Refresh Button
                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PetrolBackground)
                        .border(1.dp, PetrolBorder, RoundedCornerShape(10.dp))
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PetrolBrandGreen,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh prices",
                            tint = PetrolBrandGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Crisp 1dp hairline bottom separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PetrolBorder)
            )

            // Compact contextual warning alert
            if (isOutsideUk) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PetrolPriceExpensive.copy(alpha = 0.15f))
                        .clickable { onSearchClick() }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PetrolPriceExpensive)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Outside UK · Tap to search UK town or postcode",
                        color = PetrolPrimaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else if (isLocationDenied) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PetrolSurfaceElevated)
                        .clickable { onSearchClick() }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = PetrolSecondaryText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Location is off · Tap to search",
                        color = PetrolPrimaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else if (isOffline) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PetrolSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = PetrolSecondaryText,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "No internet connection (Offline)",
                        color = PetrolSecondaryText,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
