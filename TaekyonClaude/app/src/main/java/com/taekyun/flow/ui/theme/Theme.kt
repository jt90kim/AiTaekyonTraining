package com.taekyun.flow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TaekyonClaudeTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkTaekyonColors() else lightTaekyonColors()
    val colorScheme = if (colors.isDark) {
        darkColorScheme(
            primary          = colors.accent,
            onPrimary        = colors.accentInk,
            background       = colors.bg,
            onBackground     = colors.fg,
            surface          = colors.surface,
            onSurface        = colors.fg,
            surfaceVariant   = colors.surface2,
            onSurfaceVariant = colors.mute,
            error            = colors.warn,
        )
    } else {
        lightColorScheme(
            primary          = colors.accent,
            onPrimary        = colors.accentInk,
            background       = colors.bg,
            onBackground     = colors.fg,
            surface          = colors.surface,
            onSurface        = colors.fg,
            surfaceVariant   = colors.surface2,
            onSurfaceVariant = colors.mute,
            error            = colors.warn,
        )
    }
    CompositionLocalProvider(LocalTaekyonColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content,
        )
    }
}
