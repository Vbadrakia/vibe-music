package com.vibe.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VibeDarkColorScheme = darkColorScheme(
    primary            = VibeGreen,
    onPrimary          = VibeTextPrimary,
    primaryContainer   = VibeGreenDark,
    onPrimaryContainer = VibeTextPrimary,
    secondary          = VibeElevated,
    onSecondary        = VibeTextPrimary,
    background         = VibeBg,
    onBackground       = VibeTextPrimary,
    surface            = VibeSurface,
    onSurface          = VibeTextPrimary,
    surfaceVariant     = VibeCard,
    onSurfaceVariant   = VibeTextSecondary,
    error              = VibeError,
    onError            = VibeTextPrimary,
    outline            = VibeBorder,
    outlineVariant     = VibeDivider
)

@Composable
fun VibeTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = VibeBg.toArgb()
            window.navigationBarColor = VibeBg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = VibeDarkColorScheme,
        typography  = VibeTypography,
        content     = content
    )
}
