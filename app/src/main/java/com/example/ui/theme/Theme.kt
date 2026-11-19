package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
    background = Obsidian,
    onBackground = Parchment,
    surface = Mahogany,
    onSurface = Parchment,
    surfaceVariant = MahoganyLight,
    onSurfaceVariant = ParchmentDim,
    outline = BurgundyBorder,
    outlineVariant = BurgundyBorderSoft,
    error = Crimson,
    onError = CrimsonText
)

@Composable
fun WarbandTranslatorTheme(
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
      colorScheme = MedievalDarkColorScheme,
      typography = AppTypography,
      content = content
  )
}
