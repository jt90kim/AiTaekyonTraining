package com.jtk.android.taekyonclaude

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.jtk.android.taekyonclaude.ui.theme.*

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TaekyonClaudeTheme {
                var showSetup by rememberSaveable { mutableStateOf(false) }
                var seconds by rememberSaveable { mutableIntStateOf(60) }
                // Store as List<String> so rememberSaveable can serialize it via Bundle
                var enabledMovesList by rememberSaveable { mutableStateOf(listOf("roundhouse_low")) }
                val enabledMoves = enabledMovesList.toSet()

                if (showSetup) {
                    SetupScreen(
                        seconds = seconds,
                        enabledMoves = enabledMoves,
                        onSecondsChange = { seconds = it },
                        onMovesChange = { enabledMovesList = it.toList() },
                        onBack = { showSetup = false },
                        onStart = {
                            startActivity(
                                Intent(this, MainActivity::class.java).apply {
                                    putExtra("durationSeconds", seconds)
                                    putExtra("enabledMoves", enabledMoves.joinToString(","))
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                            )
                        },
                    )
                } else {
                    SplashScreen(onContinue = { showSetup = true })
                }
            }
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

private fun Modifier.dashedBorder(color: androidx.compose.ui.graphics.Color, width: Dp = 1.dp, radius: Dp = 12.dp): Modifier =
    drawBehind {
        drawRoundRect(
            color = color,
            style = Stroke(
                width = width.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
            ),
            cornerRadius = CornerRadius(radius.toPx()),
        )
    }

@Composable
private fun MonoLabel(text: String, color: androidx.compose.ui.graphics.Color = Mute, size: Int = 10) {
    Text(
        text = text,
        fontFamily = GeistMonoFamily,
        fontSize = size.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        letterSpacing = 0.06.em,
    )
}

// ─── Splash ─────────────────────────────────────────────────────────────────

@Composable
private fun SplashScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // Top meta strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonoLabel("B · EMBERS")
            MonoLabel("v0.4.1")
        }

        // Center brand block
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
        ) {
            // Hangul accent
            Text(
                "결련택견",
                fontFamily = NotoSansKRFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                letterSpacing = 0.04.em,
            )

            Spacer(Modifier.height(14.dp))

            // Wordmark
            Text(
                "TAEKYON",
                fontFamily = SpaceGroteskFamily,
                fontSize = 64.sp,
                fontWeight = FontWeight.SemiBold,
                color = Fg,
                letterSpacing = (-0.04).em,
                lineHeight = (64 * 0.92).sp,
            )

            // Accent rule
            Spacer(Modifier.height(16.dp))
            Box(Modifier.width(48.dp).height(2.dp).background(Accent))

            // Subtitle
            Spacer(Modifier.height(14.dp))
            Text(
                "Trainer · Korean martial reaction drill",
                fontFamily = SpaceGroteskFamily,
                fontSize = 17.sp,
                color = Mute,
            )

            // About card
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Surface)
                    .border(1.dp, Line, RoundedCornerShape(18.dp))
                    .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 16.dp)
            ) {
                Column {
                    MonoLabel("About", size = 10)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Watch the opponent. React in real time.\nNo scoring. No pose tracking. Just rhythm.",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 13.sp,
                        color = Mute,
                        lineHeight = (13 * 1.55).sp,
                    )
                }
            }
        }

        // Primary CTA
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 30.dp)
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = AccentInk),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            ) {
                Text(
                    "Begin training",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.01.em,
                )
            }
        }
    }
}

// ─── Setup ──────────────────────────────────────────────────────────────────

@Composable
private fun SetupScreen(
    seconds: Int,
    enabledMoves: Set<String>,
    onSecondsChange: (Int) -> Unit,
    onMovesChange: (Set<String>) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val canStart = enabledMoves.isNotEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(20.dp)) {
                    val s = size
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(s.width * 0.625f, s.height * 0.2f)
                            lineTo(s.width * 0.325f, s.height * 0.5f)
                            lineTo(s.width * 0.625f, s.height * 0.8f)
                        },
                        color = Fg,
                        style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }

            // Title
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Session setup",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Fg,
                    letterSpacing = (-0.01).em,
                )
                Text(
                    "훈련 준비",
                    fontFamily = NotoSansKRFamily,
                    fontSize = 10.sp,
                    color = Mute2,
                    letterSpacing = 0.04.em,
                )
            }

            Spacer(Modifier.width(32.dp))
        }

        // Scrollable body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            DurationSection(seconds, onSecondsChange)
            Spacer(Modifier.height(26.dp))
            TechniquesSection(enabledMoves, onMovesChange)
        }

        // Footer CTA
        Column(
            Modifier
                .fillMaxWidth()
                .background(Bg)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp)
        ) {
            Button(
                onClick = { if (canStart) onStart() },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = AccentInk,
                    disabledContainerColor = Surface2,
                    disabledContentColor = Mute2,
                ),
            ) {
                Text(
                    if (canStart) "Start · ${MotionLibrary.fmtMSS(seconds)}"
                    else "Pick at least one variant",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─── Duration section ────────────────────────────────────────────────────────

@Composable
private fun DurationSection(seconds: Int, onSecondsChange: (Int) -> Unit) {
    val clamp = { s: Int -> s.coerceIn(15, 300) }

    // Section label row
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        MonoLabel("Duration", color = Mute, size = 11)
        MonoLabel("30s – 5m", color = Mute2, size = 10)
    }

    Spacer(Modifier.height(12.dp))

    // Big display card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, Line, RoundedCornerShape(18.dp))
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton("–15s") { onSecondsChange(clamp(seconds - 15)) }

            Text(
                MotionLibrary.fmtMSS(seconds),
                fontFamily = GeistMonoFamily,
                fontSize = 64.sp,
                fontWeight = FontWeight.SemiBold,
                color = Fg,
                letterSpacing = (-0.04).em,
            )

            StepperButton("+15s") { onSecondsChange(clamp(seconds + 15)) }
        }
    }

    Spacer(Modifier.height(10.dp))

    // Preset chips
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MotionLibrary.durationPresets.forEach { preset ->
            val active = seconds == preset
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) Accent else androidx.compose.ui.graphics.Color.Transparent)
                    .border(1.dp, if (active) Accent else Line, RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSecondsChange(preset) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    MotionLibrary.fmtMSS(preset),
                    fontFamily = GeistMonoFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) AccentInk else Fg,
                    letterSpacing = 0.04.em,
                )
            }
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Line, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = GeistMonoFamily, fontSize = 11.sp, color = Mute, letterSpacing = 0.04.em)
    }
}

// ─── Techniques section ──────────────────────────────────────────────────────

@Composable
private fun TechniquesSection(enabledMoves: Set<String>, onMovesChange: (Set<String>) -> Unit) {
    val readyCount = MotionLibrary.techniques
        .flatMap { it.heights }
        .count { it.status == Status.Ready }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        MonoLabel("Techniques", color = Mute, size = 11)
        MonoLabel("${enabledMoves.size} selected", color = Mute2, size = 10)
    }

    Spacer(Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MotionLibrary.techniques.forEach { tech ->
            TechniqueCard(tech, enabledMoves, onMovesChange)
        }
    }

    Spacer(Modifier.height(16.dp))

    // Footer hint card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(Line, radius = 12.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("◆", fontFamily = GeistMonoFamily, fontSize = 10.sp, color = Accent)
        Text(
            "New variants unlock as motion clips are captured. Current build: $readyCount ready · 7 planned.",
            fontFamily = GeistMonoFamily,
            fontSize = 10.sp,
            color = Mute2,
            lineHeight = (10 * 1.5).sp,
            letterSpacing = 0.02.em,
        )
    }
}

@Composable
private fun TechniqueCard(
    tech: TechniqueFamily,
    enabledMoves: Set<String>,
    onMovesChange: (Set<String>) -> Unit,
) {
    val anyOn = tech.heights.any { it.id in enabledMoves }
    val isReady = tech.status == Status.Ready

    if (!isReady) {
        // Compact planned row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.55f)
                .clip(RoundedCornerShape(18.dp))
                .background(Surface)
                .border(1.dp, Line, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(tech.name, fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Fg)
            Text(tech.hangul, fontFamily = NotoSansKRFamily, fontSize = 12.sp, color = Mute)
            Text("· ${tech.romaja}", fontFamily = GeistMonoFamily, fontSize = 10.sp, color = Mute2, letterSpacing = 0.05.em)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, Line, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("soon", fontFamily = GeistMonoFamily, fontSize = 9.sp, color = Mute2, letterSpacing = 0.22.em)
            }
        }
        return
    }

    // Full ready card — tap to toggle all ready variants in this family
    val readyIds = tech.heights.filter { it.status == Status.Ready }.map { it.id }.toSet()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (anyOn) AccentDim else Surface)
            .border(1.dp, if (anyOn) Accent else Line, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onMovesChange(if (anyOn) enabledMoves - readyIds else enabledMoves + readyIds)
            }
            .padding(14.dp)
    ) {
        Column {
            // Title row with checkmark indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(tech.name, fontFamily = SpaceGroteskFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Fg, letterSpacing = (-0.01).em)
                    Text(tech.hangul, fontFamily = NotoSansKRFamily, fontSize = 13.sp, color = Mute)
                    Text("· ${tech.romaja}", fontFamily = GeistMonoFamily, fontSize = 10.sp, color = Mute2, letterSpacing = 0.05.em)
                }
                // On/off indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (anyOn) Accent else androidx.compose.ui.graphics.Color.Transparent)
                        .border(1.5.dp, if (anyOn) Accent else LineStrong, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (anyOn) {
                        Canvas(Modifier.size(11.dp)) {
                            val sc = size.width / 12f
                            drawPath(
                                path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(2.5f * sc, 6.5f * sc)
                                    lineTo(5.0f * sc, 9.0f * sc)
                                    lineTo(9.5f * sc, 3.5f * sc)
                                },
                                color = AccentInk,
                                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(tech.desc, fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = Mute, lineHeight = (12 * 1.4).sp)
        }
    }
}

