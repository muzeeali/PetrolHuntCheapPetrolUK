package com.zeetech.petrolmap.fuelpricesuk.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.R
import com.zeetech.petrolmap.fuelpricesuk.data.local.PreferencesManager
import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.data.model.NavigationApp
import com.zeetech.petrolmap.fuelpricesuk.data.model.SortMode
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolError
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurface
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated

@Composable
fun SettingsScreen(
    currentFuel: FuelType,
    currentRadius: Int,
    currentSort: SortMode,
    currentNavApp: NavigationApp,
    isLocationGranted: Boolean,
    lastUpdatedTime: String?,
    onFuelClick: () -> Unit,
    onRadiusClick: () -> Unit,
    onSortClick: () -> Unit,
    onNavAppChanged: (NavigationApp) -> Unit,
    onRefreshPrices: () -> Unit,
    onClearSavedData: () -> Unit
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }
    var infoDialogTitle by remember { mutableStateOf<String?>(null) }
    var infoDialogMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetrolBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Settings",
                    color = PetrolPrimaryText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customise your telemetry & feed parameters",
                    color = PetrolSecondaryText,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PetrolSurfaceElevated)
                    .border(1.dp, PetrolBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PetrolBrandGreen)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "SYNC ACTIVE",
                        color = PetrolBrandGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Top App Identity Card (Unified single hero section for brand logo & app info)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PetrolSurfaceElevated),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PetrolBorder))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Petrol Map Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Petrol Map",
                        color = PetrolPrimaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "UK Forecourts & Live Fuel Prices",
                        color = PetrolSecondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PetrolBrandGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "v1.0.0",
                        color = PetrolBrandGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION A: PREFERENCES
        SectionHeader(icon = Icons.Default.Tune, title = "PREFERENCES")

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.LocalGasStation,
                title = "Default Fuel",
                value = currentFuel.label,
                onClick = onFuelClick
            )

            SettingsRow(
                icon = Icons.Default.MyLocation,
                title = "Search Radius",
                value = "$currentRadius miles",
                onClick = onRadiusClick
            )

            SettingsRow(
                icon = Icons.Default.Sort,
                title = "Default Sorting",
                value = currentSort.label,
                onClick = onSortClick,
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION B: NAVIGATION
        SectionHeader(icon = Icons.Default.MyLocation, title = "NAVIGATION")

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PetrolSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PetrolBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Preferred Route App",
                    color = PetrolSecondaryText,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NavigationApp.values().forEach { app ->
                        val isSelected = app == currentNavApp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PetrolBrandGreen else PetrolSurface)
                                .border(1.dp, if (isSelected) PetrolBrandGreen else PetrolBorder, RoundedCornerShape(10.dp))
                                .clickable { onNavAppChanged(app) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (app) {
                                    NavigationApp.SYSTEM_DEFAULT -> "Default"
                                    NavigationApp.GOOGLE_MAPS -> "Google Maps"
                                    NavigationApp.WAZE -> "Waze"
                                },
                                color = if (isSelected) PetrolBackground else PetrolPrimaryText,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION C: FUEL DATA
        SectionHeader(icon = Icons.Default.Public, title = "FUEL DATA")

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Info,
                title = "Price Status",
                value = if (lastUpdatedTime != null) "Updated" else "Live",
                onClick = {}
            )

            SettingsRow(
                icon = Icons.Default.Refresh,
                title = "Refresh Prices",
                value = "Sync",
                onClick = {
                    onRefreshPrices()
                    Toast.makeText(context, "Fetching latest UK fuel prices...", Toast.LENGTH_SHORT).show()
                }
            )

            SettingsRow(
                icon = Icons.Default.DeleteOutline,
                title = "Clear Saved Data",
                value = "Clear",
                valueColor = PetrolError,
                onClick = { showClearConfirm = true },
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION D: SUPPORT & FEEDBACK
        SectionHeader(icon = Icons.Default.ThumbUp, title = "SUPPORT & COMMUNITY")

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Star,
                title = "Rate on Google Play",
                value = "Review",
                valueColor = PetrolBrandGreen,
                onClick = {
                    val act = context as? android.app.Activity
                    val userPrefs = PreferencesManager(context)
                    if (act != null) {
                        com.zeetech.petrolmap.fuelpricesuk.utils.InAppReviewHelper.launchDirectReview(act, userPrefs)
                    } else {
                        val packageName = context.packageName
                        try {
                            val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                            }
                            context.startActivity(rateIntent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                            context.startActivity(webIntent)
                        }
                    }
                }
            )

            SettingsRow(
                icon = Icons.Default.Share,
                title = "Share Petrol Map",
                value = "Share",
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Find the cheapest petrol & diesel prices across the UK with Petrol Map: https://play.google.com/store/apps/details?id=${context.packageName}"
                        )
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Petrol Map"))
                }
            )

            SettingsRow(
                icon = Icons.Default.Email,
                title = "Report Price / Feedback",
                value = "Contact",
                onClick = {
                    val emailSubject = "[Petrol Map UK] Price Discrepancy & Feedback"
                    val emailBody = """
Hello Petrol Map Team,

I would like to report a fuel price discrepancy / share feedback:

• Station / Brand: 
• Location / Postcode: 
• Fuel Type (E10, B7, etc.): 
• Price Shown in App: 
• Actual Pump Price: 
• Comments / Feedback: 

---
App Version: 1.0.0
Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:muzeeali8@gmail.com?subject=${Uri.encode(emailSubject)}&body=${Uri.encode(emailBody)}")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No email app found. Reach us at muzeeali8@gmail.com", Toast.LENGTH_LONG).show()
                    }
                },
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION E: LEGAL & ABOUT (Single unified section, no duplicate logo/identity card)
        SectionHeader(icon = Icons.Default.Security, title = "LEGAL & ABOUT")

        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.MyLocation,
                title = "Location Permission",
                value = if (isLocationGranted) "Granted" else "Disabled",
                valueColor = if (isLocationGranted) PetrolBrandGreen else PetrolError,
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )

            SettingsRow(
                icon = Icons.Default.Lock,
                title = "Privacy Policy",
                value = "View",
                onClick = {
                    infoDialogTitle = "Privacy Policy"
                    infoDialogMessage = "Petrol Map: UK Fuel Prices does not collect or sell your personal data.\n\n• Location Data: Used strictly on-device to query nearby forecourts and calculate distances.\n• Fuel Queries: Anonymized requests sent through our Cloudflare API edge proxy.\n• Local Preferences: Search history and station caches remain strictly on your device.\n• No Account: Petrol Map requires no registration or personal account."
                }
            )

            SettingsRow(
                icon = Icons.Default.Description,
                title = "Terms of Use",
                value = "View",
                onClick = {
                    infoDialogTitle = "Terms of Use"
                    infoDialogMessage = "Petrol Map provides fuel price comparison as an informational convenience utility.\n\nPrices are sourced via open public data under the UK Fuel Finder & CMA Open Data Scheme. While forecourts update regularly, pump prices at point of sale may vary.\n\nAlways verify at the pump.\n\nAlways drive safely and observe UK road traffic laws."
                }
            )

            SettingsRow(
                icon = Icons.Default.Public,
                title = "Fuel Data Attribution",
                value = "gov.uk",
                valueColor = PetrolBrandGreen,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gov.uk/government/collections/fuel-finder"))
                    context.startActivity(intent)
                }
            )

            SettingsRow(
                icon = Icons.Default.Info,
                title = "Disclaimer",
                value = "View",
                onClick = {
                    infoDialogTitle = "Disclaimer"
                    infoDialogMessage = "Fuel Price Disclaimer:\nPrices are sourced via open public data provided under the UK Fuel Finder & CMA Open Data Scheme. While forecourts update regularly, pump prices at point of sale may vary. Always verify at the pump.\n\nGovernment Non-Affiliation:\nPetrol Map is an independent fuel price tool and is not affiliated with, sponsored by, or endorsed by the UK Government or any government agency. Fuel price data is sourced from open public data provided under the UK Fuel Finder scheme."
                },
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Crafted for UK Drivers · Drive smart, save per litre",
            color = PetrolBrandGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Clear Data Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text(
                    text = "Clear saved data?",
                    color = PetrolPrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This removes saved station results and local search history. Your preferences will be retained.",
                    color = PetrolSecondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearSavedData()
                        showClearConfirm = false
                        Toast.makeText(context, "Saved data cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear data", color = PetrolError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = PetrolSecondaryText)
                }
            },
            containerColor = PetrolSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Informational Dialog (Privacy, Terms, Disclaimer)
    if (infoDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { infoDialogTitle = null },
            title = {
                Text(
                    text = infoDialogTitle ?: "",
                    color = PetrolPrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = infoDialogMessage ?: "",
                        color = PetrolSecondaryText,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { infoDialogTitle = null }) {
                    Text("Close", color = PetrolBrandGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = PetrolSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PetrolBrandGreen,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = PetrolMutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PetrolSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PetrolBorder))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = PetrolPrimaryText,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PetrolSecondaryText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = PetrolPrimaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = PetrolMutedText,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PetrolBorder)
        )
    }
}
