package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val MedievalGothicFontFamily = FontFamily(
    Font(R.font.medievalsharp, FontWeight.Normal),
    Font(R.font.medievalsharp, FontWeight.Bold)
)

val CinzelFontFamily = FontFamily(
    Font(R.font.cinzel, FontWeight.Bold)
)

val CrimsonProFontFamily = FontFamily(
    Font(R.font.crimson_pro, FontWeight.Normal),
    Font(R.font.crimson_pro, FontWeight.Normal, FontStyle.Italic)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

fun buildAppTypography(medievalTheme: Boolean): Typography {
  val displayFamily = if (medievalTheme) MedievalGothicFontFamily else InterFontFamily
  val sectionFamily = if (medievalTheme) CinzelFontFamily else InterFontFamily
  val bodyFamily = if (medievalTheme) CrimsonProFontFamily else InterFontFamily
  val uiLabelFamily = if (medievalTheme) CrimsonProFontFamily else InterFontFamily

  return Typography(
      headlineLarge = TextStyle(
          fontFamily = displayFamily,
          fontWeight = FontWeight.Bold,
          fontSize = if (medievalTheme) 29.sp else 26.sp,
          lineHeight = 35.sp,
          letterSpacing = 0.6.sp
      ),
      headlineMedium = TextStyle(
          fontFamily = displayFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 22.sp,
          lineHeight = 28.sp,
          letterSpacing = 0.5.sp
      ),
      titleMedium = TextStyle(
          fontFamily = sectionFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          lineHeight = 20.sp,
          letterSpacing = if (medievalTheme) 0.8.sp else 0.2.sp
      ),
      titleSmall = TextStyle(
          fontFamily = sectionFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp,
          lineHeight = 18.sp
      ),
      bodyLarge = TextStyle(
          fontFamily = bodyFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 16.sp,
          lineHeight = 22.sp
      ),
      bodyMedium = TextStyle(
          fontFamily = bodyFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 14.sp,
          lineHeight = 20.sp
      ),
      bodySmall = TextStyle(
          fontFamily = bodyFamily,
          fontWeight = FontWeight.Normal,
          fontSize = 13.sp,
          lineHeight = 18.sp
      ),
      labelLarge = TextStyle(
          fontFamily = sectionFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          lineHeight = 18.sp
      ),
      labelMedium = TextStyle(
          fontFamily = uiLabelFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          lineHeight = 16.sp
      ),
      labelSmall = TextStyle(
          fontFamily = uiLabelFamily,
          fontWeight = FontWeight.Medium,
          fontSize = 11.sp,
          lineHeight = 14.sp
      )
  )
}

val AppTypography: Typography = buildAppTypography(medievalTheme = true)
