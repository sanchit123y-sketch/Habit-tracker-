package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = PureWhite,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldDarker,
    secondary = EmeraldBright,
    onSecondary = PureWhite,
    secondaryContainer = EmeraldLight,
    onSecondaryContainer = EmeraldDarker,
    tertiary = SubjectMathsColor,
    onTertiary = PureWhite,
    background = MintSurface,
    onBackground = TextOnLightPrimary,
    surface = PureWhite,
    onSurface = TextOnLightPrimary,
    surfaceVariant = EmeraldContainer,
    onSurfaceVariant = TextOnLightSecondary,
    outline = SoftBorder,
    outlineVariant = SoftBorder,
    error = ErrorRed,
    onError = PureWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = EmeraldDarker,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = TextOnDarkPrimary,
    secondary = EmeraldBright,
    onSecondary = PureWhite,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextOnDarkPrimary,
    tertiary = SubjectMathsColor,
    onTertiary = PureWhite,
    background = DarkBackground,
    onBackground = TextOnDarkPrimary,
    surface = DarkSurface,
    onSurface = TextOnDarkPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextOnDarkSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = ErrorRed,
    onError = PureWhite
)

val StudyLeafShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun StudyLeafTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StudyLeafShapes,
        content = content
    )
}
