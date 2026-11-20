package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val HormozganColorScheme = lightColorScheme(
    primary = GulfTurquoise,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = DeepGulfTeal,
    secondary = WarmAmberGold,
    onSecondary = WarmSepiaInk,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = WarmSepiaInk,
    tertiary = TerracottaOchre,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = CrimsonCliff,
    background = ParchmentCream,
    onBackground = WarmSepiaInk,
    surface = ParchmentCream,
    onSurface = WarmSepiaInk,
    surfaceVariant = ParchmentAged,
    onSurfaceVariant = SoftWoodBrown,
    outline = ParchmentBorder,
    error = CrimsonCliff,
    onError = Color.White,
)

private val HormozganShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun HormozganTheme(
    content: @Composable () -> Unit,
) {
    // Enforce global RTL layout and Vazirmatn default text style across all UI components
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = HormozganColorScheme,
            typography = HormozganTypography,
            shapes = HormozganShapes,
        ) {
            ProvideTextStyle(
                value = TextStyle(
                    fontFamily = VazirmatnFontFamily,
                    textDirection = TextDirection.Rtl,
                ),
                content = content,
            )
        }
    }
}
