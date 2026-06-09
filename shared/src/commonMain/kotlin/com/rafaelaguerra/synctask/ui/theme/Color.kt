package com.rafaelaguerra.synctask.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand palette ──────────────────────────────────────────────────────────
// Calm teal as the signature so the app feels deliberate (not gray neutral).
val TealDeep = Color(0xFF2F6B6E)
val Teal = Color(0xFF3F8C90)
val TealSoft = Color(0xFFCFE4E6)
val TealMist = Color(0xFFE7F2F2)
val TealContainerDark = Color(0xFF1F3E40)
val TealOnContainerDark = Color(0xFFC9E4E5)

// Warm accent for secondary CTAs / illustrations.
val Clay = Color(0xFFB7836D)
val ClaySoft = Color(0xFFF0DDD3)
val ClayContainerDark = Color(0xFF4A3830)
val ClayOnContainerDark = Color(0xFFF6E6DD)

// Tertiary cool blue (mode card highlight).
val Indigo = Color(0xFF5871A6)
val IndigoSoft = Color(0xFFDDE4F3)
val IndigoContainerDark = Color(0xFF2A3550)
val IndigoOnContainerDark = Color(0xFFDDE4F3)

// ─── Neutrals ───────────────────────────────────────────────────────────────
val WarmCream = Color(0xFFF8F5EF)
val LightSurface = Color(0xFFFEFCF8)
val LightSurfaceVariant = Color(0xFFEFEAE0)
val LightOutline = Color(0xFFD8D2C5)
val WarmGray = Color(0xFF8E877B)
val Charcoal = Color(0xFF1F2326)

val DarkBackground = Color(0xFF12181A)
val DarkSurface = Color(0xFF181F22)
val DarkSurfaceVariant = Color(0xFF1F272A)
val DarkOutline = Color(0xFF364045)
val DarkOnSurface = Color(0xFFE7ECEE)
val DarkOnSurfaceVariant = Color(0xFFA6B0B4)

val White = Color(0xFFFFFFFF)

// ─── Mode card colors (used on the event list) ──────────────────────────────
// Light variants.
val ModeNormalLight = Color(0xFFDDE4F3)      // Normal → indigo soft
val ModeVibrateLight = Color(0xFFDCE7D6)     // Vibrate → green soft
val ModeSilentLight = Color(0xFF3F484C)      // Silent → charcoal
val ModeDndLight = Color(0xFFF0DDD3)         // DND → clay soft
val ModeAirplaneLight = Color(0xFFCFE4E6)    // Airplane → teal soft

// Dark variants (slightly desaturated, still legible on dark bg).
val ModeNormalDark = Color(0xFF2A3550)
val ModeVibrateDark = Color(0xFF2D4035)
val ModeSilentDark = Color(0xFF1A1F22)
val ModeDndDark = Color(0xFF4A3830)
val ModeAirplaneDark = Color(0xFF1F3E40)

// ─── Backwards-compat aliases (older code references these names) ───────────
val Sage = Teal
val SageDeep = TealDeep
val SageSoft = TealSoft
val MistBlue = Indigo
val MistBlueSoft = IndigoSoft
val Sand = LightOutline
val SoftBeige = LightSurfaceVariant
val CharcoalSoft = Color(0xFF2A2F33)

val Ink50 = Color(0xFFF4F6EE)
val Ink100 = Color(0xFFE5E8DE)
val Ink200 = Color(0xFFC8CCC1)
val Ink300 = Color(0xFFA5A99F)
val Ink400 = Color(0xFF74776E)
val Ink500 = Color(0xFF595C53)
val Ink600 = Color(0xFF42443D)
val Ink700 = Color(0xFF2E302B)
val Ink800 = Color(0xFF232420)
val Ink900 = Color(0xFF1B1C1A)
val Ink950 = Color(0xFF151513)

val VioletGlow = Teal
val VioletLight = TealSoft
val VioletDark = TealDeep
val VioletContainer = TealContainerDark
val VioletGhost = Color(0xFF1B2F30)

val CyanGlow = Clay
val CyanLight = ClaySoft
val CyanGhost = Color(0xFF3F2A22)

val MintGlow = Indigo
val MintLight = IndigoSoft
val MintGhost = Color(0xFF1F2A40)

val LightBg = WarmCream
val LightSurfVar = LightSurfaceVariant
val LightBorder = LightOutline

// Legacy mode-card aliases (still imported in older modules).
val CardPurple = ModeNormalLight
val CardLime = ModeVibrateLight
val CardNavy = ModeSilentLight
val CardCoral = ModeDndLight
val CardLavender = ModeAirplaneLight
