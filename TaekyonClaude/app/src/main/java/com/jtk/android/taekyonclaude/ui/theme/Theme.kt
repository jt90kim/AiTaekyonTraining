package com.jtk.android.taekyonclaude.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TaekyonColorScheme = darkColorScheme(
    primary = TaekyonOrange,
    background = TaekyonBackground,
    surface = TaekyonSurface,
    onPrimary = TaekyonTextPrimary,
    onBackground = TaekyonTextPrimary,
    onSurface = TaekyonTextPrimary,
)

@Composable
fun TaekyonClaudeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TaekyonColorScheme,
        typography = Typography,
        content = content
    )
}
