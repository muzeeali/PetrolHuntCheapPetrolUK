package com.zeetech.petrolmap.fuelpricesuk.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.R
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated

@Composable
fun FirstLaunchLocationScreen(
    onRequestLocation: () -> Unit,
    onSearchManually: () -> Unit
) {
    val context = LocalContext.current
    val privacyUrl = "https://zeetechsolutionspvt.blogspot.com/2026/10/petrol-map-uk-fuel-prices.html"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetrolBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // App Logo
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Petrol Map Logo",
            modifier = Modifier.size(110.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Find Cheap Fuel Near You",
            color = PetrolPrimaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Compare live forecourt prices across the UK and find the nearest petrol and diesel pumps.",
            color = PetrolSecondaryText,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Prominent Location Disclosure Card (Google Play Policy Compliance)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PetrolSurfaceElevated),
            border = BorderStroke(1.dp, PetrolBrandGreen.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = PetrolBrandGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Location Access Disclosure",
                        color = PetrolPrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Petrol Map accesses device location data (approximate and precise) only while the app is actively in use in the foreground to discover nearby forecourts, calculate travel distances, and display live fuel prices in your area.\n\nYour location is processed locally in real time on your device, is never stored on our servers, and is never tracked in the background.",
                    color = PetrolSecondaryText,
                    fontSize = 12.sp,
                    lineHeight = 16.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRequestLocation,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PetrolBrandGreen,
                contentColor = PetrolBackground
            ),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "Agree & enable location",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onSearchManually,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PetrolPrimaryText
            ),
            border = BorderStroke(1.dp, PetrolBorder),
            shape = RoundedCornerShape(23.dp)
        ) {
            Text(
                text = "Search manually without location",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Privacy Policy",
            color = PetrolBrandGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
                } catch (_: Exception) {}
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
