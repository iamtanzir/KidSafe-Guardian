package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Minimal Technical Dark Theme (uBlockDNS style)
val BackgroundDark = Color(0xFF09090B)   // Almost pure black
val SurfaceDark = Color(0xFF121214)      // Card surface
val BorderDark = Color(0xFF27272A)       // Subtle borders
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFFA1A1AA)

// Accents
val AccentRed = Color(0xFFF87171)        // Blocked / Coral Red
val AccentGreen = Color(0xFF4ADE80)      // Allowed / Active Green
val AccentBrand = Color(0xFFF4F4F5)      // Primary buttons (White/Light Grey)

// Backward compatibility mappings
val DeepTeal = AccentBrand
val CoralWarm = AccentRed
val SoftCream = BackgroundDark
val DarkCharcoal = TextSecondary
val SoftGold = Color(0xFFFBBF24)
val SafeGreen = AccentGreen
val AlertRed = AccentRed

// System colors
val CharcoalDark = BackgroundDark
val LightTeal = AccentBrand
val TealDark = SurfaceDark
