package com.zeetech.petrolmap.fuelpricesuk.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Apple Design System Animation, Physics, & Optical Typography Tokens.
 * Distilled from Apple's "Designing Fluid Interfaces" & design guidelines:
 * - Damping ratio 1.0 (critically damped) for calm spatial moves
 * - Damping ratio ~0.8 (momentum spring) for physical interactions/sheets
 * - Optical size-specific tracking: negative tracking on large text, positive on captions
 * - Response on pointer-down with tactile spring recovery
 */
object AppleMotion {
    // Apple-spec Physics Springs
    val CriticallyDampedFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val MomentumSpringFloat = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMediumLow
    )

    val SnappySpringFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val CriticallyDampedColor = spring<androidx.compose.ui.graphics.Color>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Optical Typography Tracking Tokens
    val DisplayLargeTracking = (-1.0).sp
    val DisplayPriceTracking = (-0.5).sp
    val HeadingTracking = (-0.2).sp
    val BodyTracking = 0.sp
    val CaptionTracking = 0.3.sp
}

/**
 * Modifier that implements Apple's Response principle:
 * Respond on pointer-down (instant visual feedback) with tactile scale and haptic feedback,
 * followed by an interruptible spring rebound on release.
 */
fun Modifier.applePressEffect(
    scaleOnPress: Float = 0.96f,
    enableHaptic: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .pointerInput(scaleOnPress, enableHaptic, onClick) {
            detectTapGestures(
                onPress = {
                    if (enableHaptic) {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                    }
                    scope.launch {
                        scale.animateTo(scaleOnPress, AppleMotion.SnappySpringFloat)
                    }
                    val released = tryAwaitRelease()
                    scope.launch {
                        scale.animateTo(1f, AppleMotion.MomentumSpringFloat)
                    }
                    if (released && onClick != null) {
                        onClick()
                    }
                }
            )
        }
}
