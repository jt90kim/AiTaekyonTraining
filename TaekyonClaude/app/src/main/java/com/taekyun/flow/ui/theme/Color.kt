package com.taekyun.flow.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TaekyonColors(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val lineStrong: Color,
    val fg: Color,
    val mute: Color,
    val mute2: Color,
    val accent: Color,
    val accentDim: Color,
    val accentInk: Color,
    val warn: Color,
)

fun darkTaekyonColors() = TaekyonColors(
    isDark     = true,
    bg         = Color(0xFF100C08),
    surface    = Color(0xFF1A140E),
    surface2   = Color(0xFF241B12),
    line       = Color(0x1AFFDCAA),
    lineStrong = Color(0x40FFDCAA),
    fg         = Color(0xFFF6EAD6),
    mute       = Color(0x8CF6EAD6),
    mute2      = Color(0x52F6EAD6),
    accent     = Color(0xFFFFAA2B),
    accentDim  = Color(0x29FFAA2B),
    accentInk  = Color(0xFF1A0E00),
    warn       = Color(0xFFFF6A3A),
)

fun lightTaekyonColors() = TaekyonColors(
    isDark     = false,
    bg         = Color(0xFFF6F1EA),
    surface    = Color(0xFFFFFFFF),
    surface2   = Color(0xFFEFE8DC),
    line       = Color(0x1F3C280A),
    lineStrong = Color(0x523C280A),
    fg         = Color(0xFF1A120A),
    mute       = Color(0x991A120A),
    mute2      = Color(0x591A120A),
    accent     = Color(0xFFC66A00),
    accentDim  = Color(0x26C66A00),
    accentInk  = Color(0xFFFFFFFF),
    warn       = Color(0xFF9C4413),
)

val LocalTaekyonColors = staticCompositionLocalOf { darkTaekyonColors() }
