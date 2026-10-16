package com.zeetech.petrolmap.fuelpricesuk.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import com.zeetech.petrolmap.fuelpricesuk.data.network.PetrolMapApiClient
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurface
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated

@Composable
fun StationDetailDialog(
    station: Station,
    currentFuel: FuelType,
    searchRadius: Int,
    onDismiss: () -> Unit,
    onGetDirections: (Station) -> Unit,
    onViewOnMap: (Station) -> Unit
) {
    val context = LocalContext.current

    var currentPrices by remember(station.id) { mutableStateOf(station.prices) }
    var currentUpdatedDates by remember(station.id) { mutableStateOf(station.fuelUpdatedDates) }
    var activePrice by remember(station.id) { mutableDoubleStateOf(station.prices[currentFuel.code] ?: station.price) }
    var activeUpdatedText by remember(station.id) {
        mutableStateOf(station.fuelUpdatedDates[currentFuel.code] ?: station.updatedAt)
    }

    LaunchedEffect(station.slug) {
        if (station.slug.isNotBlank()) {
            val live = PetrolMapApiClient.fetchStationLiveDetails(station.slug)
            if (live != null) {
                currentPrices = currentPrices + live.prices
                currentUpdatedDates = currentUpdatedDates + live.fuelUpdatedDates
                if (live.prices.containsKey(currentFuel.code)) {
                    activePrice = live.prices[currentFuel.code] ?: activePrice
                }
                if (live.fuelUpdatedDates.containsKey(currentFuel.code)) {
                    activeUpdatedText = live.fuelUpdatedDates[currentFuel.code] ?: activeUpdatedText
                } else if (live.latestUpdated.isNotBlank()) {
                    activeUpdatedText = live.latestUpdated
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, PetrolBorder, RoundedCornerShape(20.dp)),
            color = PetrolBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Back button and Station Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(PetrolSurfaceElevated, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PetrolPrimaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = station.name,
                            color = PetrolPrimaryText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PetrolBrandGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Verified Forecourt · ${station.brand}",
                                color = PetrolSecondaryText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Price Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PetrolSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PetrolBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PetrolBorder)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentFuel.label,
                                    color = PetrolBrandGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", station.distanceMiles)} mi away",
                                color = PetrolSecondaryText,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", activePrice),
                                color = PetrolBrandGreen,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " p/L",
                                color = PetrolBrandGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PetrolBrandGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified price within your $searchRadius mi search radius",
                                color = PetrolPrimaryText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (activeUpdatedText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatUpdateTime(activeUpdatedText),
                                color = PetrolBrandGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fuel Grades Section
                Text(
                    text = "Fuel Grades",
                    color = PetrolPrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FuelType.entries.forEach { fuel ->
                    val isCurrent = fuel == currentFuel
                    val fuelPrice = currentPrices[fuel.code] ?: if (isCurrent) activePrice else null
                    val fuelUpdated = currentUpdatedDates[fuel.code] ?: if (isCurrent && activeUpdatedText.isNotBlank()) activeUpdatedText else null

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrent) PetrolSurfaceElevated else PetrolSurface)
                            .border(1.dp, if (isCurrent) PetrolBrandGreen.copy(alpha = 0.5f) else PetrolBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                tint = if (isCurrent) PetrolBrandGreen else PetrolMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = fuel.label,
                                    color = PetrolPrimaryText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = fuel.sublabel,
                                    color = PetrolMutedText,
                                    fontSize = 11.sp
                                )
                                if (!fuelUpdated.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = fuelUpdated,
                                        color = PetrolBrandGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (fuelPrice != null && fuelPrice > 0) String.format(java.util.Locale.US, "%.1fp", fuelPrice) else "Unavailable",
                            color = if (isCurrent) PetrolBrandGreen else if (fuelPrice != null) PetrolPrimaryText else PetrolMutedText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Address & Forecourt Info
                Text(
                    text = "Forecourt Location",
                    color = PetrolPrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PetrolSurfaceElevated)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = PetrolSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val fullAddress = listOf(
                            station.address.line1,
                            station.address.town,
                            station.address.postcode
                        ).filter { it.isNotEmpty() }.joinToString(", ")

                        Text(
                            text = fullAddress.ifEmpty { "Location details available via GPS" },
                            color = PetrolPrimaryText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Station Address", station.address.line1)
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy address",
                            tint = PetrolSecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onGetDirections(station)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PetrolBrandGreen,
                        contentColor = PetrolBackground
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get Directions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onViewOnMap(station)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PetrolPrimaryText
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PetrolBorder),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = PetrolSecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Center on Map",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                com.zeetech.petrolmap.fuelpricesuk.ui.components.FuelFinderDisclaimer(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun formatUpdateTime(raw: String): String {
    if (raw.isBlank()) return "Recently updated"
    if (raw.startsWith("Last updated", ignoreCase = true)) return raw
    return try {
        if (raw.contains("/")) {
            // format: 12/10/2026 00:00:01
            val parts = raw.split(" ")
            "Updated ${parts.firstOrNull() ?: raw}"
        } else if (raw.contains("T")) {
            // ISO format: 2026-10-12T12:00:00Z
            "Updated ${raw.substringBefore("T")}"
        } else {
            "Updated $raw"
        }
    } catch (e: Exception) {
        "Recently updated"
    }
}
