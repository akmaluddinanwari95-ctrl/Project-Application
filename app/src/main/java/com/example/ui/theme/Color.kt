package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Obsidian = Color(0xFF120A0A)
val ObsidianDeep = Color(0xFF100707)
val Mahogany = Color(0xFF1D1110)
val MahoganyCardTop = Color(0xFF1E1211)
val MahoganyCardBottom = Color(0xFF160D0C)
val MahoganyLight = Color(0xFF281715)
val MahoganyInput = Color(0xFF221413)
val MahoganyHeader = Color(0xFF140B0B)

val Burgundy = Color(0xFF663A32)
val BurgundyHover = Color(0xFF7E483E)
val BurgundyBorder = Color(0xFF4A2A24)
val BurgundyBorderSoft = Color(0xFF3A201C)

val Parchment = Color(0xFFEADCC2)
val ParchmentDim = Color(0xFFC2B194)

val GoldAccent = Color(0xFFD4AF37)
val GoldMuted = Color(0xFF9A7D4A)

val Forest = Color(0xFF2E5A27)
val ForestText = Color(0xFFBBE6B3)
val Crimson = Color(0xFF8B2635)
val CrimsonText = Color(0xFFFCA5A5)
val AmberText = Color(0xFFFCD34D)
val AmberBg = Color(0xFF2A1D12)

@Immutable
data class WarbandPalette(
    val isMedievalTheme: Boolean,
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val parchmentGlow: Color,
    val headerBg: Color,
    val cardTop: Color,
    val cardBottom: Color,
    val cardElevated: Color,
    val inputBg: Color,
    val deepSurface: Color,
    val primaryAccent: Color,
    val primaryMuted: Color,
    val actionBg: Color,
    val border: Color,
    val borderSoft: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val successBg: Color,
    val successBorder: Color,
    val successText: Color,
    val warningBg: Color,
    val warningText: Color,
    val errorBg: Color,
    val errorBorder: Color,
    val errorText: Color
)

val MedievalParchmentPalette = WarbandPalette(
    isMedievalTheme = true,
    bgTop = Color(0xFF1F140E),
    bgMid = Color(0xFF170E0A),
    bgBottom = Color(0xFF0E0806),
    parchmentGlow = Color(0xFF5D3A24),
    headerBg = Color(0xFF160D09),
    cardTop = Color(0xFF251711),
    cardBottom = Color(0xFF190F0B),
    cardElevated = Color(0xFF311F16),
    inputBg = Color(0xFF261712),
    deepSurface = Color(0xFF110A08),
    primaryAccent = Color(0xFFD4AF37),
    primaryMuted = Color(0xFF9A7D4A),
    actionBg = Color(0xFF6E3D2E),
    border = Color(0xFF5A3727),
    borderSoft = Color(0xFF3F251B),
    textPrimary = Color(0xFFF1E3C6),
    textSecondary = Color(0xFFCBB692),
    successBg = Color(0xFF0E2914),
    successBorder = Forest,
    successText = ForestText,
    warningBg = AmberBg,
    warningText = AmberText,
    errorBg = Color(0xFF331015),
    errorBorder = Crimson,
    errorText = CrimsonText
)

val StandardDarkPalette = WarbandPalette(
    isMedievalTheme = false,
    bgTop = Color(0xFF11161F),
    bgMid = Color(0xFF0E1219),
    bgBottom = Color(0xFF0A0D12),
    parchmentGlow = Color(0xFF1E3A5F),
    headerBg = Color(0xFF11161F),
    cardTop = Color(0xFF1A222E),
    cardBottom = Color(0xFF141A24),
    cardElevated = Color(0xFF232E3D),
    inputBg = Color(0xFF171F2A),
    deepSurface = Color(0xFF0B0F15),
    primaryAccent = Color(0xFF60A5FA),
    primaryMuted = Color(0xFF3B82F6),
    actionBg = Color(0xFF1D4ED8),
    border = Color(0xFF334155),
    borderSoft = Color(0xFF243041),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    successBg = Color(0xFF0D2B1B),
    successBorder = Color(0xFF22C55E),
    successText = Color(0xFF86EFAC),
    warningBg = Color(0xFF2B2110),
    warningText = Color(0xFFFCD34D),
    errorBg = Color(0xFF311219),
    errorBorder = Color(0xFFEF4444),
    errorText = Color(0xFFFCA5A5)
)

val LocalWarbandPalette = staticCompositionLocalOf { MedievalParchmentPalette }
