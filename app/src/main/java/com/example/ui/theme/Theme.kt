package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Immutable
data class CausaSuiColors(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val cardElevated: Color,
    val text: Color,
    val textMuted: Color,
    val workout: Color,
    val onWorkout: Color,
    val water: Color,
    val onWater: Color,
    val nutrition: Color,
    val onNutrition: Color,
    val progress: Color,
    val onProgress: Color,
    val rankGold: Color,
    val onRankGold: Color,
    val sage: Color,
    val peach: Color,
    val navCapsule: Color,
    val navInactive: Color,
)

private val DarkCausaColors = CausaSuiColors(
    isDark = true,
    background = DarkBackground,
    card = DarkCard,
    cardElevated = DarkCardElevated,
    text = DarkText,
    textMuted = DarkTextMuted,
    workout = DarkWorkout,
    onWorkout = Color(0xFFF4F2EE),
    water = DarkWater,
    onWater = Color(0xFFF4F2EE),
    nutrition = DarkNutrition,
    onNutrition = Color(0xFF1E1E22),
    progress = DarkProgress,
    onProgress = Color(0xFFF4F2EE),
    rankGold = DarkRank,
    onRankGold = Color(0xFF1E1E22),
    sage = Color(0xFF4F6B54),
    peach = Color(0xFFB57256),
    navCapsule = NavCapsuleBg,
    navInactive = NavIconInactive,
)

private val BeigeCausaColors = CausaSuiColors(
    isDark = false,
    background = BeigeBackground,
    card = BeigeCard,
    cardElevated = BeigeCardElevated,
    text = BeigeText,
    textMuted = BeigeTextMuted,
    workout = BeigeWorkout,
    onWorkout = Color(0xFFF7F1E3),
    water = BeigeWater,
    onWater = Color(0xFFF7F1E3),
    nutrition = BeigeNutrition,
    onNutrition = Color(0xFF1E1E22),
    progress = BeigeProgress,
    onProgress = Color(0xFFF7F1E3),
    rankGold = BeigeRank,
    onRankGold = Color(0xFF1E1E22),
    sage = BeigeSage,
    peach = BeigePeach,
    navCapsule = Color(0xFF1E1E22),
    navInactive = Color(0xFF8C877E),
)

val LocalCausaSuiColors = staticCompositionLocalOf { BeigeCausaColors }

val CausaSuiShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun CausaSuiTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val causaColors = if (darkTheme) DarkCausaColors else BeigeCausaColors

    val materialScheme = if (darkTheme) {
        darkColorScheme(
            primary = causaColors.workout,
            onPrimary = causaColors.onWorkout,
            secondary = causaColors.water,
            onSecondary = causaColors.onWater,
            tertiary = causaColors.progress,
            onTertiary = causaColors.onProgress,
            background = causaColors.background,
            onBackground = causaColors.text,
            surface = causaColors.card,
            onSurface = causaColors.text,
            surfaceVariant = causaColors.cardElevated,
            onSurfaceVariant = causaColors.textMuted,
        )
    } else {
        lightColorScheme(
            primary = causaColors.workout,
            onPrimary = causaColors.onWorkout,
            secondary = causaColors.water,
            onSecondary = causaColors.onWater,
            tertiary = causaColors.progress,
            onTertiary = causaColors.onProgress,
            background = causaColors.background,
            onBackground = causaColors.text,
            surface = causaColors.card,
            onSurface = causaColors.text,
            surfaceVariant = causaColors.cardElevated,
            onSurfaceVariant = causaColors.textMuted,
        )
    }

    CompositionLocalProvider(LocalCausaSuiColors provides causaColors) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = CausaSuiTypography,
            shapes = CausaSuiShapes,
            content = content,
        )
    }
}
