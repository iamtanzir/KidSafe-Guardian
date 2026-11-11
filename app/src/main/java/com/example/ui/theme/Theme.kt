package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LightTeal,
    secondary = CoralWarm,
    tertiary = SoftGold,
    background = CharcoalDark,
    surface = SurfaceDark,
    onPrimary = CharcoalDark,
    onSecondary = CharcoalDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = AlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = DeepTeal,
    secondary = CoralWarm,
    tertiary = SoftGold,
    background = SoftCream,
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = DarkCharcoal,
    onSurface = DarkCharcoal,
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force true to ensure the spectacular Cyber Hacker Dark styling is consistent across all screens
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Ensure status bar is deep hacker obsidian black and icons are light (white)
                window.statusBarColor = CharcoalDark.toArgb()
                window.navigationBarColor = CharcoalDark.toArgb()
                
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
