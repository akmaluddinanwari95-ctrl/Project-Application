package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MedievalDarkColorScheme = darkColorScheme(
    primary = Burgundy,
    onPrimary = Parchment,
    primaryContainer = BurgundyHover,
    onPrimaryContainer = Parchment,
    secondary = GoldAccent,
    onSecondary = Obsidian,
    secondaryContainer = AmberBg,
    onSecondaryContainer = GoldAccent,
    tertiary = Forest,
    onTertiary = ForestText,
    background = MedievalParchmentPalette.bgMid,
    onBackground = MedievalParchmentPalette.textPrimary,
    surface = MedievalParchmentPalette.cardTop,
    onSurface = MedievalParchmentPalette.textPrimary,
    surfaceVariant = MedievalParchmentPalette.cardElevated,
    onSurfaceVariant = MedievalParchmentPalette.textSecondary,
    outline = MedievalParchmentPalette.border,
    outlineVariant = MedievalParchmentPalette.borderSoft,
    error = Crimson,
    onError = CrimsonText
)

private val StandardDarkColorScheme = darkColorScheme(
    primary = StandardDarkPalette.actionBg,
    onPrimary = StandardDarkPalette.textPrimary,
    primaryContainer = StandardDarkPalette.cardElevated,
    onPrimaryContainer = StandardDarkPalette.textPrimary,
    secondary = StandardDarkPalette.primaryAccent,
    onSecondary = StandardDarkPalette.bgBottom,
    secondaryContainer = StandardDarkPalette.warningBg,
    onSecondaryContainer = StandardDarkPalette.primaryAccent,
    tertiary = Forest,
    onTertiary = StandardDarkPalette.successText,
    background = StandardDarkPalette.bgMid,
    onBackground = StandardDarkPalette.textPrimary,
    surface = StandardDarkPalette.cardTop,
    onSurface = StandardDarkPalette.textPrimary,
    surfaceVariant = StandardDarkPalette.cardElevated,
    onSurfaceVariant = StandardDarkPalette.textSecondary,
    outline = StandardDarkPalette.border,
    outlineVariant = StandardDarkPalette.borderSoft,
    error = StandardDarkPalette.errorBorder,
    onError = StandardDarkPalette.errorText
)

@Composable
fun WarbandTranslatorTheme(
    medievalTheme: Boolean = true,
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

  val palette = if (medievalTheme) MedievalParchmentPalette else StandardDarkPalette
  val colorScheme = if (medievalTheme) MedievalDarkColorScheme else StandardDarkColorScheme
  val typography = buildAppTypography(medievalTheme)

  CompositionLocalProvider(LocalWarbandPalette provides palette) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
  }
}
