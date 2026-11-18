package com.example.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DeadzoneColorScheme = darkColorScheme(
    primary = ToxicAcidLime,
    onPrimary = ObsidianAsphalt,
    primaryContainer = ToxicLimeDim,
    onPrimaryContainer = TacticalWhite,
    secondary = CyberCyan,
    onSecondary = ObsidianAsphalt,
    secondaryContainer = CyberCyanDim,
    onSecondaryContainer = TacticalWhite,
    tertiary = HazardAmber,
    onTertiary = ObsidianAsphalt,
    error = BioCrimson,
    onError = TacticalWhite,
    background = ObsidianAsphalt,
    onBackground = TacticalWhite,
    surface = CarbonPlate,
    onSurface = TacticalWhite,
    surfaceVariant = BunkerSurface,
    onSurfaceVariant = TacticalMuted,
    outline = SteelBorder
)

private val DeadzoneShapes = Shapes(
    small = CutCornerShape(4.dp),
    medium = CutCornerShape(8.dp),
    large = CutCornerShape(12.dp)
)

@Composable
fun DeadzoneProtocolTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DeadzoneColorScheme,
        typography = DeadzoneTypography,
        shapes = DeadzoneShapes,
        content = content
    )
}
