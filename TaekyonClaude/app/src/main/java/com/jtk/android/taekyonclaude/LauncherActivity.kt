package com.jtk.android.taekyonclaude

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.jtk.android.taekyonclaude.ui.theme.*

private val SPLASH_DURATION_MS = 1500L

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val startTime = SystemClock.elapsedRealtime()
        splashScreen.setKeepOnScreenCondition {
            SystemClock.elapsedRealtime() - startTime < SPLASH_DURATION_MS
        }
        splashScreen.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .setDuration(600L)
                .withEndAction { provider.remove() }
                .start()
        }

        setContent {
            TaekyonClaudeTheme {
                var seconds by rememberSaveable { mutableIntStateOf(60) }
                var enabledMovesList by rememberSaveable { mutableStateOf(listOf("roundhouse_low")) }
                val enabledMoves = enabledMovesList.toSet()

                SetupScreen(
                    seconds = seconds,
                    enabledMoves = enabledMoves,
                    onSecondsChange = { seconds = it },
                    onMovesChange = { enabledMovesList = it.toList() },
                    onBack = { },
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
            }
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

private fun Modifier.dashedBorder(color: Color, width: Dp = 1.dp, radius: Dp = 12.dp): Modifier =
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
private fun MonoLabel(text: String, color: Color? = null, size: Int = 10) {
    val c = LocalTaekyonColors.current
    Text(
        text = text,
        fontFamily = GeistMonoFamily,
        fontSize = size.sp,
        fontWeight = FontWeight.Medium,
        color = color ?: c.mute,
        letterSpacing = 0.06.em,
    )
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
    val c = LocalTaekyonColors.current
    val canStart = enabledMoves.isNotEmpty()

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                        color = c.fg,
                        style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.setup_title),
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.fg,
                    letterSpacing = (-0.01).em,
                )
                Text(
                    "훈련 준비",
                    fontFamily = NotoSansKRFamily,
                    fontSize = 10.sp,
                    color = c.mute2,
                    letterSpacing = 0.04.em,
                )
            }
            Spacer(Modifier.width(32.dp))
        }

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

        Column(
            Modifier
                .fillMaxWidth()
                .background(c.bg)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp)
        ) {
            Button(
                onClick = { if (canStart) onStart() },
                enabled = canStart,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.accent,
                    contentColor = c.accentInk,
                    disabledContainerColor = c.surface2,
                    disabledContentColor = c.mute2,
                ),
            ) {
                Text(
                    if (canStart) stringResource(R.string.start_button, MotionLibrary.fmtMSS(seconds))
                    else stringResource(R.string.start_pick_variant),
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
    val c = LocalTaekyonColors.current
    val clamp = { s: Int -> s.coerceIn(15, 300) }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        MonoLabel(stringResource(R.string.duration_label), size = 11)
        MonoLabel(stringResource(R.string.duration_range), color = c.mute2, size = 10)
    }

    Spacer(Modifier.height(12.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(c.surface)
            .border(1.dp, c.line, RoundedCornerShape(18.dp))
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(stringResource(R.string.stepper_minus)) { onSecondsChange(clamp(seconds - 15)) }
            Text(
                MotionLibrary.fmtMSS(seconds),
                fontFamily = GeistMonoFamily,
                fontSize = 64.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.fg,
                letterSpacing = (-0.04).em,
            )
            StepperButton(stringResource(R.string.stepper_plus)) { onSecondsChange(clamp(seconds + 15)) }
        }
    }

    Spacer(Modifier.height(10.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MotionLibrary.durationPresets.forEach { preset ->
            val active = seconds == preset
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) c.accent else Color.Transparent)
                    .border(1.dp, if (active) c.accent else c.line, RoundedCornerShape(12.dp))
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
                    color = if (active) c.accentInk else c.fg,
                    letterSpacing = 0.04.em,
                )
            }
        }
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    val c = LocalTaekyonColors.current
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, c.line, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = GeistMonoFamily, fontSize = 11.sp, color = c.mute, letterSpacing = 0.04.em)
    }
}

// ─── Techniques section ──────────────────────────────────────────────────────

@Composable
private fun TechniquesSection(enabledMoves: Set<String>, onMovesChange: (Set<String>) -> Unit) {
    val c = LocalTaekyonColors.current
    val allHeights = MotionLibrary.techniques.flatMap { it.heights }
    val readyCount = allHeights.count { it.status == Status.Ready }
    val soonCount  = allHeights.count { it.status == Status.Soon }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        MonoLabel(stringResource(R.string.techniques_label), size = 11)
        MonoLabel(stringResource(R.string.techniques_selected, enabledMoves.size), color = c.mute2, size = 10)
    }

    Spacer(Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MotionLibrary.techniques.forEach { tech ->
            TechniqueCard(tech, enabledMoves, onMovesChange)
        }
    }

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(c.line, radius = 12.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("◆", fontFamily = GeistMonoFamily, fontSize = 10.sp, color = c.accent)
        Text(
            stringResource(R.string.techniques_note, readyCount, soonCount),
            fontFamily = GeistMonoFamily,
            fontSize = 10.sp,
            color = c.mute2,
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
    val c = LocalTaekyonColors.current
    val anyOn = tech.heights.any { it.id in enabledMoves }

    if (tech.status != Status.Ready) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.55f)
                .clip(RoundedCornerShape(18.dp))
                .background(c.surface)
                .border(1.dp, c.line, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(tech.nameResId), fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.fg)
            Text(tech.hangul, fontFamily = NotoSansKRFamily, fontSize = 12.sp, color = c.mute)
            Text("· ${tech.romaja}", fontFamily = GeistMonoFamily, fontSize = 10.sp, color = c.mute2, letterSpacing = 0.05.em)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, c.line, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(stringResource(R.string.technique_soon_badge), fontFamily = GeistMonoFamily, fontSize = 9.sp, color = c.mute2, letterSpacing = 0.22.em)
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (anyOn) c.accentDim else c.surface)
            .border(1.dp, if (anyOn) c.accent else c.line, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(stringResource(tech.nameResId), fontFamily = SpaceGroteskFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.fg, letterSpacing = (-0.01).em)
                Text(tech.hangul, fontFamily = NotoSansKRFamily, fontSize = 13.sp, color = c.mute)
                Text("· ${tech.romaja}", fontFamily = GeistMonoFamily, fontSize = 10.sp, color = c.mute2, letterSpacing = 0.05.em)
            }
            Spacer(Modifier.height(6.dp))
            Text(stringResource(tech.descResId), fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = c.mute, lineHeight = (12 * 1.4).sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tech.heights.forEach { height ->
                    val on = height.id in enabledMoves
                    HeightChip(height = height, on = on) {
                        onMovesChange(if (on) enabledMoves - height.id else enabledMoves + height.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeightChip(height: HeightVariant, on: Boolean, onToggle: () -> Unit) {
    val c = LocalTaekyonColors.current
    val isReady = height.status == Status.Ready
    val chipBg     = if (on) c.accent else Color.Transparent
    val chipBorder = if (on) c.accent else if (isReady) c.lineStrong else c.line
    val textColor  = if (on) c.accentInk else if (isReady) c.fg else c.mute2
    val dotBorder  = if (on) c.accentInk else if (isReady) c.lineStrong else c.line
    val dotBg      = if (on) c.accentInk else Color.Transparent

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(chipBg)
            .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
            .alpha(if (isReady) 1f else 0.6f)
            .then(
                if (isReady) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onToggle() } else Modifier
            )
            .padding(start = 8.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(dotBg)
                    .border(1.5.dp, dotBorder, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (on) {
                    Canvas(Modifier.size(9.dp)) {
                        val sc = size.width / 12f
                        drawPath(
                            path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(2.5f * sc, 6.5f * sc)
                                lineTo(5.0f * sc, 9.0f * sc)
                                lineTo(9.5f * sc, 3.5f * sc)
                            },
                            color = c.accent,
                            style = Stroke(width = 2.4f * sc, cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }
                }
            }
            Text(
                stringResource(height.labelResId),
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
            Text(
                if (isReady) "· ${height.variants}V" else "· SOON",
                fontFamily = GeistMonoFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (on) c.accentInk else c.mute2,
                letterSpacing = 0.1.em,
                modifier = Modifier.alpha(0.75f),
            )
        }
    }
}
