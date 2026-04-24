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
    private val _durationSeconds = mutableIntStateOf(180)
    private val _sessionKey = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _durationSeconds.intValue = intent.getIntExtra("durationSeconds", 180)

        addContentView(
            ComposeView(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setContent {
                    TaekyonClaudeTheme {
                        key(_sessionKey.intValue) {
                            TrainingOverlay(
                                durationSeconds = _durationSeconds.intValue,
                                unityReady = _unityReady.value
                            )
                        }
                    }
                }
            },
            android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        )
    }

    // Called when a new "Start Training" intent arrives while activity is already alive
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _durationSeconds.intValue = intent.getIntExtra("durationSeconds", 180)
        _sessionKey.intValue++  // Forces TrainingOverlay to recreate with fresh state
        // Unity scene is still loaded; signal ready so timer starts immediately
        runOnUiThread { _unityReady.value = true }
    }

    // Called from Unity's AndroidBridge.Start() once the scene is loaded on first launch
    fun onUnitySceneReady() {
        runOnUiThread { _unityReady.value = true }
    }

    private fun navigateBack() {
        // Bring LauncherActivity's task to the foreground without destroying this activity.
        // Calling finish() here would trigger UnityPlayerGameActivity.onDestroy() which calls
        // System.exit() from native code — killing the whole process.
        startActivity(
            Intent(this, LauncherActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
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
