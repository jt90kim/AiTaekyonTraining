package com.taekyun.flow

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.taekyun.flow.ui.theme.*
import com.unity3d.player.UnityPlayer
import com.unity3d.player.UnityPlayerGameActivity
import kotlinx.coroutines.delay

class MainActivity : UnityPlayerGameActivity() {

    private val _unityReady = mutableStateOf(false)
    private val _isPaused = mutableStateOf(false)
    private val _showExitDialog = mutableStateOf(false)
    private val _countdownValue = mutableIntStateOf(-1) // -1: idle, 3→2→1→0: counting
    private val _durationSeconds = mutableIntStateOf(180)
    private val _sessionKey = mutableIntStateOf(0)
    private var _enabledMovesCsv = "roundhouse_low"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        _durationSeconds.intValue = intent.getIntExtra("durationSeconds", 180)
        _enabledMovesCsv = intent.getStringExtra("enabledMoves") ?: "roundhouse_low"

        addContentView(
            ComposeView(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setContent {
                    TaekyonClaudeTheme {
                        key(_sessionKey.intValue) {
                            TrainingOverlay(
                                durationSeconds = _durationSeconds.intValue,
                                unityReady = _unityReady.value,
                                enabledMovesCsv = _enabledMovesCsv,
                            )
                        }
                    }
                }
            },
            android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT),
        )
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        if (!_unityReady.value) return
        if (_countdownValue.intValue > 0) {
            // Reset countdown so it starts from 3 again when the user returns
            _countdownValue.intValue = 3
        } else if (!_isPaused.value) {
            _isPaused.value = true
            sendPaused(true)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _durationSeconds.intValue = intent.getIntExtra("durationSeconds", 180)
        _enabledMovesCsv = intent.getStringExtra("enabledMoves") ?: "roundhouse_low"
        _sessionKey.intValue++
        _isPaused.value = false
        _showExitDialog.value = false
        runOnUiThread {
            _countdownValue.intValue = 3
            sendEnabledMoves()
            sendPaused(true)
            _unityReady.value = true
        }
    }

    fun onUnitySceneReady() {
        runOnUiThread {
            _countdownValue.intValue = 3
            sendEnabledMoves()
            sendPaused(true)
            _unityReady.value = true
        }
    }

    private fun sendEnabledMoves() {
        try {
            UnityPlayer.UnitySendMessage("AndroidBridge", "SetEnabledMoves", _enabledMovesCsv)
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "SetEnabledMoves failed: ${e.message}")
        }
    }

    private fun sendPaused(paused: Boolean) {
        try {
            UnityPlayer.UnitySendMessage("AndroidBridge", "SetPaused", if (paused) "true" else "false")
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "SetPaused failed: ${e.message}")
        }
    }

    private fun navigateBack() {
        startActivity(
            Intent(this, LauncherActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
        moveTaskToBack(true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when {
                _countdownValue.intValue > 0 -> navigateBack()
                _unityReady.value && !_showExitDialog.value -> {
                    _isPaused.value = true
                    sendPaused(true)
                    _showExitDialog.value = true
                }
                !_unityReady.value -> navigateBack()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun formatTimer(seconds: Int): String {
        val s = seconds.coerceAtLeast(0)
        return "%d:%02d".format(s / 60, s % 60)
    }

    @Composable
    private fun moveLabelFor(id: String): String {
        val family = MotionLibrary.techniques.firstOrNull { t -> t.heights.any { h -> h.id == id } }
        val height = MotionLibrary.techniques.flatMap { it.heights }.firstOrNull { it.id == id }
        return if (family != null && height != null) {
            "${stringResource(family.nameResId)} ${stringResource(height.labelResId)}"
        } else {
            id.split("_").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
        }
    }

    @Composable
    private fun TrainingOverlay(durationSeconds: Int, unityReady: Boolean, enabledMovesCsv: String) {
        val c = LocalTaekyonColors.current
        var remainingSeconds by remember { mutableIntStateOf(durationSeconds) }
        var kickCount by remember { mutableIntStateOf(0) }
        val isPaused = _isPaused.value
        val showExitDialog = _showExitDialog.value
        val countdownValue = _countdownValue.intValue
        val overTime = remainingSeconds <= 0

        LaunchedEffect(unityReady, isPaused, countdownValue) {
            if (!unityReady || isPaused || countdownValue > 0) return@LaunchedEffect
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
        }

        LaunchedEffect(unityReady, overTime, isPaused, countdownValue) {
            if (!unityReady || overTime || isPaused || countdownValue > 0) return@LaunchedEffect
            while (true) {
                delay(1800L)
                kickCount++
            }
        }

        // Tick the countdown; unpause Unity when it reaches 0
        LaunchedEffect(countdownValue) {
            if (countdownValue <= 0) return@LaunchedEffect
            delay(1000L)
            // Guard: onPause may have reset the value while we were waiting
            if (_countdownValue.intValue != countdownValue) return@LaunchedEffect
            _countdownValue.intValue = countdownValue - 1
            if (countdownValue == 1) sendPaused(false)
        }

        // Auto-unpause when session ends so the mannequin keeps running under the card
        LaunchedEffect(overTime) {
            if (overTime && _isPaused.value) {
                _isPaused.value = false
                _showExitDialog.value = false
                sendPaused(false)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {

            // Three-pill top chrome
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                // Exit button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(c.surface)
                        .border(1.dp, c.line, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { navigateBack() },
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.size(16.dp)) {
                        val sw = 1.6.dp.toPx()
                        drawLine(c.fg, Offset(0f, 0f), Offset(size.width, size.height), sw, StrokeCap.Round)
                        drawLine(c.fg, Offset(size.width, 0f), Offset(0f, size.height), sw, StrokeCap.Round)
                    }
                }

                // Timer pill
                val timerColor = if (overTime || remainingSeconds < 10) c.warn else c.fg
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 140.dp)
                        .clip(RoundedCornerShape(50))
                        .background(c.surface)
                        .border(1.dp, if (overTime) c.warn else c.line, RoundedCornerShape(50))
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val statusLabel = when {
                            overTime -> stringResource(R.string.training_status_time_up)
                            unityReady -> stringResource(R.string.training_status_active)
                            else -> stringResource(R.string.training_status_loading)
                        }
                        Text(
                            statusLabel,
                            fontFamily = GeistMonoFamily,
                            fontSize = 9.sp,
                            color = if (overTime) c.warn else c.mute2,
                            letterSpacing = 0.15.em,
                        )
                        Text(
                            formatTimer(remainingSeconds),
                            fontFamily = GeistMonoFamily,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = timerColor,
                            letterSpacing = (-0.02).em,
                        )
                    }
                }

                // Kicks pill
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 44.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(c.surface)
                        .border(1.dp, c.line, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.training_kicks_label),
                            fontFamily = GeistMonoFamily,
                            fontSize = 9.sp,
                            color = c.mute2,
                            letterSpacing = 0.15.em,
                        )
                        Text(
                            kickCount.toString().padStart(2, '0'),
                            fontFamily = GeistMonoFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (overTime) c.warn else c.accent,
                        )
                    }
                }
            }

            // Loading veil with spinner
            AnimatedVisibility(visible = !unityReady, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(c.bg.copy(alpha = 0.87f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "spinner")
                        val spinAngle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart,
                            ),
                            label = "spin",
                        )
                        Canvas(Modifier.size(40.dp)) {
                            val r = size.minDimension / 2f - 1.dp.toPx()
                            val sw = 2.dp.toPx()
                            drawCircle(color = c.line, radius = r, style = Stroke(width = sw))
                            drawArc(
                                color = c.accent,
                                startAngle = spinAngle - 90f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = sw, cap = StrokeCap.Round),
                            )
                        }
                        Text(
                            stringResource(R.string.training_preparing),
                            fontFamily = GeistMonoFamily,
                            fontSize = 11.sp,
                            color = c.mute,
                            letterSpacing = 0.18.em,
                        )
                    }
                }
            }

            // Countdown overlay — 3, 2, 1 before training starts
            AnimatedVisibility(
                visible = unityReady && countdownValue > 0,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(600)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(c.bg.copy(alpha = 0.60f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        countdownValue.toString(),
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.fg,
                        letterSpacing = (-0.04).em,
                    )
                }
            }

            // Pause / resume pill — bottom center, amber for visibility over checkerboard
            if (unityReady && !overTime && countdownValue <= 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(c.accent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            _isPaused.value = !_isPaused.value
                            sendPaused(_isPaused.value)
                        }
                        .padding(horizontal = 28.dp, vertical = 14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Canvas(Modifier.size(16.dp)) {
                            if (isPaused) {
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(size.width * 0.28f, size.height * 0.18f)
                                    lineTo(size.width * 0.28f, size.height * 0.82f)
                                    lineTo(size.width * 0.82f, size.height * 0.50f)
                                    close()
                                }
                                drawPath(path, color = c.accentInk)
                            } else {
                                val barW = size.width * 0.28f
                                val barH = size.height * 0.70f
                                val top  = (size.height - barH) / 2f
                                drawRect(c.accentInk, topLeft = Offset(size.width * 0.18f, top), size = androidx.compose.ui.geometry.Size(barW, barH))
                                drawRect(c.accentInk, topLeft = Offset(size.width * 0.54f, top), size = androidx.compose.ui.geometry.Size(barW, barH))
                            }
                        }
                        Text(
                            if (isPaused) stringResource(R.string.training_resume)
                            else stringResource(R.string.training_pause),
                            fontFamily = GeistMonoFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.accentInk,
                            letterSpacing = 0.06.em,
                        )
                    }
                }
            }

            // Exit confirmation dialog
            AnimatedVisibility(visible = showExitDialog, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(c.bg.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(c.surface)
                            .border(1.dp, c.line, RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            stringResource(R.string.training_exit_title),
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.fg,
                            letterSpacing = (-0.01).em,
                        )
                        Text(
                            stringResource(R.string.training_exit_body),
                            fontFamily = GeistMonoFamily,
                            fontSize = 13.sp,
                            color = c.mute,
                            letterSpacing = 0.02.em,
                        )
                        // Keep training (primary)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(CircleShape)
                                .background(c.accent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    _showExitDialog.value = false
                                    _isPaused.value = false
                                    sendPaused(false)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(R.string.training_exit_keep),
                                fontFamily = GeistMonoFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = c.accentInk,
                                letterSpacing = 0.05.em,
                            )
                        }
                        // End session (secondary)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(CircleShape)
                                .border(1.dp, c.line, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { navigateBack() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(R.string.training_exit_end),
                                fontFamily = GeistMonoFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = c.fg,
                                letterSpacing = 0.05.em,
                            )
                        }
                    }
                }
            }

            // Session complete card
            AnimatedVisibility(visible = overTime, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(c.bg.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(c.surface)
                            .border(1.dp, c.accent, RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c.accent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Canvas(Modifier.size(16.dp)) {
                                    val sw = 2.dp.toPx()
                                    drawLine(
                                        color = c.accentInk,
                                        start = Offset(size.width * 0.15f, size.height * 0.52f),
                                        end = Offset(size.width * 0.42f, size.height * 0.78f),
                                        strokeWidth = sw,
                                        cap = StrokeCap.Round,
                                    )
                                    drawLine(
                                        color = c.accentInk,
                                        start = Offset(size.width * 0.42f, size.height * 0.78f),
                                        end = Offset(size.width * 0.85f, size.height * 0.22f),
                                        strokeWidth = sw,
                                        cap = StrokeCap.Round,
                                    )
                                }
                            }
                            Column {
                                Text(
                                    stringResource(R.string.complete_title),
                                    fontFamily = GeistMonoFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = c.fg,
                                )
                                Text(
                                    stringResource(R.string.complete_subtitle),
                                    fontFamily = GeistMonoFamily,
                                    fontSize = 11.sp,
                                    color = c.mute,
                                    letterSpacing = 0.10.em,
                                )
                            }
                        }

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(c.surface2)
                                    .padding(12.dp),
                            ) {
                                Text(
                                    stringResource(R.string.training_duration_stat),
                                    fontFamily = GeistMonoFamily,
                                    fontSize = 9.sp,
                                    color = c.mute,
                                    letterSpacing = 0.15.em,
                                )
                                Text(
                                    formatTimer(durationSeconds),
                                    fontFamily = GeistMonoFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = c.fg,
                                    letterSpacing = (-0.02).em,
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(c.surface2)
                                    .padding(12.dp),
                            ) {
                                Text(
                                    stringResource(R.string.training_kicks_stat),
                                    fontFamily = GeistMonoFamily,
                                    fontSize = 9.sp,
                                    color = c.mute,
                                    letterSpacing = 0.15.em,
                                )
                                Text(
                                    kickCount.toString(),
                                    fontFamily = GeistMonoFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = c.accent,
                                    letterSpacing = (-0.02).em,
                                )
                            }
                        }

                        // Technique list
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                stringResource(R.string.training_technique_stat),
                                fontFamily = GeistMonoFamily,
                                fontSize = 9.sp,
                                color = c.mute,
                                letterSpacing = 0.15.em,
                            )
                            enabledMovesCsv
                                .split(",")
                                .filter { it.isNotBlank() }
                                .distinct()
                                .forEach { id ->
                                    Text(
                                        "· ${moveLabelFor(id)}",
                                        fontFamily = GeistMonoFamily,
                                        fontSize = 13.sp,
                                        color = c.fg,
                                    )
                                }
                        }

                        // Done button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(CircleShape)
                                .background(c.accent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { navigateBack() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(R.string.complete_done),
                                fontFamily = GeistMonoFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = c.accentInk,
                                letterSpacing = 0.05.em,
                            )
                        }
                    }
                }
            }
        }
    }
}
