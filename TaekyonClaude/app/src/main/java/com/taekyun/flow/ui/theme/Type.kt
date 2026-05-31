package com.taekyun.flow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.taekyun.flow.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

val SpaceGroteskFamily = FontFamily(
    Font(GoogleFont("Space Grotesk"), provider, FontWeight.Normal),
    Font(GoogleFont("Space Grotesk"), provider, FontWeight.Medium),
    Font(GoogleFont("Space Grotesk"), provider, FontWeight.SemiBold),
)

val GeistMonoFamily = FontFamily(
    Font(GoogleFont("Geist Mono"), provider, FontWeight.Normal),
    Font(GoogleFont("Geist Mono"), provider, FontWeight.Medium),
    Font(GoogleFont("Geist Mono"), provider, FontWeight.SemiBold),
)

val NotoSansKRFamily = FontFamily(
    Font(GoogleFont("Noto Sans KR"), provider, FontWeight.Normal),
    Font(GoogleFont("Noto Sans KR"), provider, FontWeight.Medium),
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 13.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = GeistMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 10.sp,
    ),
)
