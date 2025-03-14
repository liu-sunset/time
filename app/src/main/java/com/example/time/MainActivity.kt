package com.example.time

import android.os.Bundle
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.runtime.*
import com.example.time.ui.screens.CountdownScreen
import com.example.time.ui.screens.TimePickerScreen
import com.example.time.ui.theme.TimeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TimeTheme {
                var isCountdownStarted by remember { mutableStateOf(false) }
                var totalSeconds by remember { mutableStateOf(0L) }

                LaunchedEffect(isCountdownStarted) {
                    if (isCountdownStarted) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }

                AnimatedContent(
                    targetState = isCountdownStarted,
                    transitionSpec = {
                        fadeIn() + slideInVertically() with 
                        fadeOut() + slideOutVertically()
                    }
                ) { started ->
                    if (!started) {
                        TimePickerScreen { seconds ->
                            totalSeconds = seconds
                            isCountdownStarted = true
                        }
                    } else {
                        CountdownScreen(
                            totalSeconds = totalSeconds,
                            onFinish = { isCountdownStarted = false }
                        )
                    }
                }
            }
        }
    }
}