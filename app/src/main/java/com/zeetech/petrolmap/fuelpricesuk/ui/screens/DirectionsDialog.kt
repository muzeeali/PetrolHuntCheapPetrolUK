package com.zeetech.petrolmap.fuelpricesuk.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zeetech.petrolmap.fuelpricesuk.data.model.NavigationApp
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated

@Composable
fun DirectionsDialog(
    station: Station,
    selectedNavApp: NavigationApp,
    onNavAppChanged: (NavigationApp) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, PetrolBorder, RoundedCornerShape(20.dp)),
            color = PetrolBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            tint = PetrolBrandGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Get directions",
                            color = PetrolPrimaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PetrolSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Navigate to ${station.name}",
                    color = PetrolSecondaryText,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                NavigationApp.entries.forEach { app ->
                    val isSelected = app == selectedNavApp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PetrolSurfaceElevated else PetrolBackground)
                            .border(1.dp, if (isSelected) PetrolBrandGreen else PetrolBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onNavAppChanged(app)
                                launchNavigation(context, station, app)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onNavAppChanged(app)
                                launchNavigation(context, station, app)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PetrolBrandGreen,
                                unselectedColor = PetrolMutedText
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                color = PetrolPrimaryText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = when (app) {
                                    NavigationApp.SYSTEM_DEFAULT -> "Open system map selector"
                                    NavigationApp.GOOGLE_MAPS -> "Launch Google Maps navigation"
                                    NavigationApp.WAZE -> "Launch Waze GPS navigation"
                                },
                                color = PetrolMutedText,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = if (isSelected) PetrolBrandGreen else PetrolMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

fun launchNavigation(context: Context, station: Station, app: NavigationApp) {
    val lat = station.latitude
    val lng = station.longitude
    val label = Uri.encode(station.name)

    try {
        val intent = when (app) {
            NavigationApp.GOOGLE_MAPS -> {
                val uri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
            }
            NavigationApp.WAZE -> {
                val uri = Uri.parse("waze://?ll=$lat,$lng&navigate=yes")
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.waze")
                }
            }
            NavigationApp.SYSTEM_DEFAULT -> {
                val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
                Intent(Intent.ACTION_VIEW, uri)
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
        } catch (_: android.content.ActivityNotFoundException) {
            // Fallback to generic geo URI
            val fallbackUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallbackIntent)
            } catch (_: android.content.ActivityNotFoundException) {
                // Fallback to web browser Google Maps
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open map app: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
