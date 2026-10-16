package com.zeetech.petrolmap.fuelpricesuk.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.data.model.FuelType
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBackground
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolPrimaryText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurfaceElevated

@Composable
fun FuelSelectorChips(
    modifier: Modifier = Modifier,
    selectedFuel: FuelType,
    onFuelSelected: (FuelType) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FuelType.entries.forEach { fuel ->
            val isSelected = fuel == selectedFuel
            val shape = RoundedCornerShape(20.dp)

            val primaryLabel = when (fuel) {
                FuelType.DIESEL -> "Diesel"
                FuelType.SDV -> "SDV"
                else -> fuel.code
            }

            val secondaryLabel = when (fuel) {
                FuelType.DIESEL -> "B7"
                FuelType.SDV -> "Premium"
                else -> fuel.sublabel
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(shape)
                    .background(if (isSelected) PetrolBrandGreen else PetrolSurfaceElevated.copy(alpha = 0.95f))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) PetrolBrandGreen else PetrolBorder,
                        shape = shape
                    )
                    .clickable { onFuelSelected(fuel) }
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = primaryLabel,
                        color = if (isSelected) PetrolBackground else PetrolPrimaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = secondaryLabel,
                        color = if (isSelected) PetrolBackground.copy(alpha = 0.85f) else PetrolMutedText,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
