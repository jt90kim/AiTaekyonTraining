package com.jtk.android.taekyonclaude

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jtk.android.taekyonclaude.ui.theme.TaekyonBackground
import com.jtk.android.taekyonclaude.ui.theme.TaekyonClaudeTheme
import com.jtk.android.taekyonclaude.ui.theme.TaekyonOrange
import com.jtk.android.taekyonclaude.ui.theme.TaekyonSurface
import com.jtk.android.taekyonclaude.ui.theme.TaekyonTextPrimary
import com.jtk.android.taekyonclaude.ui.theme.TaekyonTextSecondary
import kotlinx.coroutines.delay

private enum class Screen { SPLASH, SETUP }

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaekyonClaudeTheme {
                var screen by remember { mutableStateOf(Screen.SPLASH) }
                when (screen) {
                    Screen.SPLASH -> SplashScreen(onFinish = { screen = Screen.SETUP })
                    Screen.SETUP -> SetupScreen()
                }
            }
        }
    }
}

@Composable
private fun SplashScreen(onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinish()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TaekyonBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(TaekyonOrange),
                contentAlignment = Alignment.Center
            ) {
                Text("T", color = TaekyonTextPrimary, fontSize = 42.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "TAEKYON TRAINER",
                color = TaekyonTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "AI TRAINING PARTNER",
                color = TaekyonTextSecondary,
                fontSize = 11.sp,
                letterSpacing = 3.sp
            )
        }
    }
}

@Composable
private fun SetupScreen() {
    val context = LocalContext.current
    var minutes by remember { mutableIntStateOf(3) }
    var seconds by remember { mutableIntStateOf(0) }
    val moves = remember { MotionLibrary.listClipNames(context) }
    val selected = remember { mutableStateListOf<String>() }

    Scaffold(
        containerColor = TaekyonBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TaekyonBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java).apply {
                            putStringArrayListExtra("selectedMoves", ArrayList(selected))
                            putExtra("durationSeconds", minutes * 60 + seconds)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaekyonOrange,
                        disabledContainerColor = Color(0xFF4A2010)
                    )
                ) {
                    Text(
                        "▶  START TRAINING  ⚡",
                        color = TaekyonTextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TaekyonOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text("T", color = TaekyonTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "TAEKYON TRAINER",
                        color = TaekyonTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "AI TRAINING PARTNER",
                        color = TaekyonTextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Duration card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TaekyonSurface)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏱", fontSize = 13.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "TRAINING DURATION",
                            color = TaekyonTextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DurationColumn(
                            value = minutes,
                            label = "MIN",
                            onInc = { if (minutes < 99) minutes++ },
                            onDec = { if (minutes > 0) minutes-- }
                        )
                        Text(
                            ":",
                            color = TaekyonTextPrimary,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        DurationColumn(
                            value = seconds,
                            label = "SEC",
                            onInc = { seconds = (seconds + 1) % 60 },
                            onDec = { seconds = if (seconds == 0) 59 else seconds - 1 }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Moves header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SELECT MOVES",
                        color = TaekyonTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    if (selected.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TaekyonOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${selected.size}",
                                color = TaekyonTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Row {
                    TextButton(onClick = { selected.clear(); selected.addAll(moves) }) {
                        Text("All", color = TaekyonTextSecondary, fontSize = 13.sp)
                    }
                    TextButton(onClick = { selected.clear() }) {
                        Text("Clear", color = TaekyonTextSecondary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Moves list
            moves.forEach { move ->
                val isSelected = move in selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF2A1810) else TaekyonSurface)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) TaekyonOrange else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { if (isSelected) selected.remove(move) else selected.add(move) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        move.replace('_', ' ').split(' ')
                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                        color = if (isSelected) TaekyonTextPrimary else TaekyonTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (isSelected) TaekyonOrange else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) TaekyonOrange else TaekyonTextSecondary,
                                shape = RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("✓", color = TaekyonTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DurationColumn(value: Int, label: String, onInc: () -> Unit, onDec: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onInc) {
            Text("+", color = TaekyonTextSecondary, fontSize = 24.sp, fontWeight = FontWeight.Light)
        }
        Text(
            text = "%02d".format(value),
            color = TaekyonTextPrimary,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = TaekyonTextSecondary, fontSize = 11.sp, letterSpacing = 2.sp)
        IconButton(onClick = onDec) {
            Text("−", color = TaekyonTextSecondary, fontSize = 24.sp, fontWeight = FontWeight.Light)
        }
    }
}
