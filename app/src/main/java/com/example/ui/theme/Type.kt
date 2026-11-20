package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.example.R

// Bundled local full-glyph Persian fonts (100% offline, zero runtime network dependency)
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Light),
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_medium, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_bold, FontWeight.ExtraBold),
)

val EstedadDisplayFamily = FontFamily(
    Font(R.font.estedad, FontWeight.Normal),
    Font(R.font.estedad, FontWeight.Medium),
    Font(R.font.estedad_bold, FontWeight.SemiBold),
    Font(R.font.estedad_bold, FontWeight.Bold),
    Font(R.font.estedad_bold, FontWeight.ExtraBold),
)

val ShabnamCleanFamily = FontFamily(
    Font(R.font.shabnam, FontWeight.Normal),
    Font(R.font.shabnam, FontWeight.Medium),
    Font(R.font.shabnam_bold, FontWeight.Bold),
)

val IbmPlexPersianFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Bold),
)

val PersianNastaliqFamily = FontFamily(
    Font(R.font.gulzar, FontWeight.Normal),
    Font(R.font.gulzar, FontWeight.Bold),
)

val NotoNastaliqFamily = FontFamily(
    Font(R.font.noto_nastaliq_urdu, FontWeight.Normal),
    Font(R.font.noto_nastaliq_urdu, FontWeight.Bold),
)

val LalezarAdventureFamily = FontFamily(
    Font(R.font.lalezar, FontWeight.Normal),
    Font(R.font.lalezar, FontWeight.Bold),
)

val MarkaziNarrativeFamily = FontFamily(
    Font(R.font.markazi_text, FontWeight.Normal),
    Font(R.font.markazi_text, FontWeight.Bold),
)

// Apply full-glyph Persian fonts across all 15 Material 3 Typography slots with RTL direction
val HormozganTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = EstedadDisplayFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 46.sp,
        textDirection = TextDirection.Rtl,
    ),
    displayMedium = TextStyle(
        fontFamily = EstedadDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 40.sp,
        textDirection = TextDirection.Rtl,
    ),
    displaySmall = TextStyle(
        fontFamily = EstedadDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 36.sp,
        textDirection = TextDirection.Rtl,
    ),
    headlineLarge = TextStyle(
        fontFamily = EstedadDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 38.sp,
        textDirection = TextDirection.Rtl,
    ),
    headlineMedium = TextStyle(
        fontFamily = EstedadDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 34.sp,
        textDirection = TextDirection.Rtl,
    ),
    headlineSmall = TextStyle(
        fontFamily = EstedadDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 28.sp,
        textDirection = TextDirection.Rtl,
    ),
    titleLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 32.sp,
        textDirection = TextDirection.Rtl,
    ),
    titleMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 27.sp,
        textDirection = TextDirection.Rtl,
    ),
    titleSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        textDirection = TextDirection.Rtl,
    ),
    bodyLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 30.sp,
        textDirection = TextDirection.Rtl,
    ),
    bodyMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 25.sp,
        textDirection = TextDirection.Rtl,
    ),
    bodySmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 21.sp,
        textDirection = TextDirection.Rtl,
    ),
    labelLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        textDirection = TextDirection.Rtl,
    ),
    labelMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        textDirection = TextDirection.Rtl,
    ),
    labelSmall = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        textDirection = TextDirection.Rtl,
    ),
)
