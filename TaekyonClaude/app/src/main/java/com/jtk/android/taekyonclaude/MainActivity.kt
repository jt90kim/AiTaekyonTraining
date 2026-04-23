package com.jtk.android.taekyonclaude

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedMoves = intent.getStringArrayListExtra("selectedMoves") ?: emptyList<String>()
        val durationSeconds = intent.getIntExtra("durationSeconds", 180)

        addContentView(
            ComposeView(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setContent {
                    TaekyonClaudeTheme {
                        TrainingOverlay(durationSeconds = durationSeconds)
                    }
                }
            },
            android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        )
    }

    @Composable
    private fun TrainingOverlay(durationSeconds: Int) {
        var remainingSeconds by remember { mutableIntStateOf(durationSeconds) }

        LaunchedEffect(durationSeconds) {
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
                    text = timerText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (done) Color(0xFFFF6633) else Color.White
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
                    Text(
                        text = "Training Complete",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
