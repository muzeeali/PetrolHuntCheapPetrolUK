package com.zeetech.petrolmap.fuelpricesuk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSecondaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.applePressEffect

val RADIUS_OPTIONS = listOf(1, 3, 5, 10, 15, 25, 50)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentRadius: Int,
    onApply: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRadius by remember { mutableIntStateOf(currentRadius) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, PetrolBorder, RoundedCornerShape(22.dp)),
            color = PetrolBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters",
                        color = PetrolPrimaryText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

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

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SEARCH RADIUS",
                    color = PetrolMutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RADIUS_OPTIONS.forEach { radius ->
                        val isSelected = radius == selectedRadius
                        val shape = RoundedCornerShape(12.dp)

                        Box(
                            modifier = Modifier
                                .clip(shape)
                                .background(if (isSelected) PetrolBrandGreen else PetrolSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) PetrolBrandGreen else PetrolBorder,
                                    shape
                                )
                                .applePressEffect(scaleOnPress = 0.94f) { selectedRadius = radius }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "$radius mi",
                                color = if (isSelected) PetrolBackground else PetrolPrimaryText,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { selectedRadius = 5 },
                        modifier = Modifier
                            .weight(1f)
                            .applePressEffect(scaleOnPress = 0.96f)
                    ) {
                        Text(
                            text = "Reset (5 mi)",
                            color = PetrolSecondaryText,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onApply(selectedRadius)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .applePressEffect(scaleOnPress = 0.96f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PetrolBrandGreen,
                            contentColor = PetrolBackground
                        ),
                        shape = RoundedCornerShape(23.dp)
                    ) {
                        Text(
                            text = "Apply filters",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
