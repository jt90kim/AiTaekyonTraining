package com.jtk.android.taekyonclaude.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EmbersDark = darkColorScheme(
    primary           = Accent,
    onPrimary         = AccentInk,
    background        = Bg,
    onBackground      = Fg,
    surface           = Surface,
    onSurface         = Fg,
    surfaceVariant    = Surface2,
    onSurfaceVariant  = Mute,
    error             = Warn,
)

@Composable
fun TaekyonClaudeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EmbersDark,
        typography  = Typography,
        content     = content,
    )
}
