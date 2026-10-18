package com.zeetech.petrolmap.fuelpricesuk.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.AppleMotion
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBorder
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolBrandGreen
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolMutedText
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.PetrolSurface
import com.zeetech.petrolmap.fuelpricesuk.ui.theme.applePressEffect

enum class AppTab {
    MAP,
    SETTINGS
}

/**
 * Full-width seamless bottom navigation bar.
 * Features:
 * - Solid, continuous PetrolSurface background covering the full width and system navigation bar area
 * - Crisp 1dp hairline border on top separating content cleanly
 * - Smooth animated pill indicators and spring-scale tab feedback
 * - Instant tactile pointer-down haptic response
 */
@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = PetrolSurface,
        border = BorderStroke(1.dp, PetrolBorder),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    label = "MAP",
                    icon = if (currentTab == AppTab.MAP) Icons.Filled.Map else Icons.Outlined.Map,
                    isSelected = currentTab == AppTab.MAP,
                    onClick = { onTabSelected(AppTab.MAP) },
                    modifier = Modifier.weight(1f)
                )

                NavBarItem(
                    label = "SETTINGS",
                    icon = if (currentTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    isSelected = currentTab == AppTab.SETTINGS,
                    onClick = { onTabSelected(AppTab.SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) PetrolBrandGreen.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = AppleMotion.CriticallyDampedColor,
        label = "navPillBg"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = AppleMotion.MomentumSpringFloat,
        label = "navIconScale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PetrolBrandGreen else PetrolMutedText,
        animationSpec = AppleMotion.CriticallyDampedColor,
        label = "navContentColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(pillBgColor)
            .applePressEffect(scaleOnPress = 0.95f, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.4.sp
            )
        }
    }
}
