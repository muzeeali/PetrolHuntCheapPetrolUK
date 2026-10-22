package com.zeetech.petrolmap.fuelpricesuk.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PetrolDarkColorScheme = darkColorScheme(
    primary = PetrolBrandGreen,
    onPrimary = PetrolBackground,
    primaryContainer = PetrolDarkGreen,
    onPrimaryContainer = PetrolPrimaryText,
    secondary = PetrolSecondaryText,
    onSecondary = PetrolPrimaryText,
    background = PetrolBackground,
    onBackground = PetrolPrimaryText,
    surface = PetrolSurface,
    onSurface = PetrolPrimaryText,
    surfaceVariant = PetrolSurfaceElevated,
    onSurfaceVariant = PetrolSecondaryText,
    outline = PetrolBorder,
    error = PetrolError,
    onError = PetrolPrimaryText
)

@Composable
fun PetrolMapFuelPricesUKTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = PetrolDarkColorScheme,
        typography = Typography,
        content = content
    )
}