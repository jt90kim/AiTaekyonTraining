package com.jtk.android.taekyonclaude

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jtk.android.taekyonclaude.ui.theme.TaekyonClaudeTheme
import com.unity3d.player.UnityPlayerGameActivity
import kotlinx.coroutines.delay

class MainActivity : UnityPlayerGameActivity() {

    private val _unityReady = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedMoves = intent.getStringArrayListExtra("selectedMoves") ?: emptyList<String>()
        val durationSeconds = intent.getIntExtra("durationSeconds", 180)

        addContentView(
            ComposeView(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setContent {
                    TaekyonClaudeTheme {
                        TrainingOverlay(
                            durationSeconds = durationSeconds,
                            unityReady = _unityReady.value
                        )
                    }
                }
            },
            android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        )
    }

    // Called from Unity's AndroidBridge.Start() once the scene is loaded
    fun onUnitySceneReady() {
        runOnUiThread { _unityReady.value = true }
    }

    private fun navigateBack() {
        startActivity(
            Intent(this, LauncherActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            navigateBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Composable
    private fun TrainingOverlay(durationSeconds: Int, unityReady: Boolean) {
        var remainingSeconds by remember { mutableIntStateOf(durationSeconds) }

        LaunchedEffect(unityReady) {
            if (!unityReady) return@LaunchedEffect
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
        }

        val done = remainingSeconds <= 0
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timerText = "%d:%02d".format(minutes, seconds)

        Box(modifier = Modifier.fillMaxSize()) {
            // Timer pill at top-center
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.Black.copy(alpha = 0.60f)
            ) {
                Text(
                    text = if (unityReady) timerText else "–:––",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (done) Color(0xFFFF6633) else Color.White
                )
            }

            // Exit button at top-right
            TextButton(
                onClick = { navigateBack() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 12.dp)
            ) {
                Text(
                    text = "✕",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.80f)
                )
            }

            // Training-over overlay
            if (done) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.70f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Training Complete",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { navigateBack() }) {
                            Text("Exit", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
