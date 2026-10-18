package com.zeetech.petrolmap.fuelpricesuk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.data.model.Station
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated

import com.zeetech.petrolmap.fuelpricesuk.ui.theme.AppleMotion
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.applePressEffect

@Composable
fun StationCard(
    modifier: Modifier = Modifier,
    station: Station,
    isCheapest: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PetrolSurfaceElevated)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) PetrolBrandGreen else PetrolBorder.copy(alpha = 0.85f),
                shape = shape
            )
            .applePressEffect(scaleOnPress = 0.98f, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand logo icon square
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PetrolBorder.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalGasStation,
                contentDescription = null,
                tint = if (isCheapest) PetrolBrandGreen else PetrolSecondaryText,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Station Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = station.name,
                    color = PetrolPrimaryText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (isCheapest) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PetrolBrandGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CHEAPEST",
                            color = PetrolBrandGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            val addressPart = station.address.line1.ifEmpty { station.address.postcode }
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", station.distanceMiles)} mi away · $addressPart",
                color = PetrolSecondaryText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (station.diffFromAvg != 0.0) {
                Spacer(modifier = Modifier.height(2.dp))
                val diffStr = if (station.diffFromAvg < 0) {
                    "${String.format(java.util.Locale.US, "%.1f", -station.diffFromAvg)}p cheaper than avg"
                } else {
                    "${String.format(java.util.Locale.US, "%.1f", station.diffFromAvg)}p vs avg"
                }
                Text(
                    text = diffStr,
                    color = if (station.diffFromAvg < 0) PetrolBrandGreen else PetrolMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Price badge column
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", station.price),
                    color = if (isCheapest) PetrolBrandGreen else PetrolPrimaryText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = AppleMotion.DisplayPriceTracking
                )
                Text(
                    text = "p",
                    color = if (isCheapest) PetrolBrandGreen else PetrolSecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
                )
            }
            Text(
                text = "${station.fuel} / litre",
                color = PetrolMutedText,
                fontSize = 11.sp
            )
            val updateText = station.updatedAt
                .replace("Last updated by Fuel Finder", "")
                .replace("Last updated by Retailer", "")
                .replace("Last updated by CMA", "")
                .replace("Last updated", "")
                .trim()
            if (updateText.isNotBlank()) {
                Text(
                    text = updateText,
                    color = PetrolMutedText,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = PetrolMutedText,
            modifier = Modifier.size(18.dp)
        )
    }
}
