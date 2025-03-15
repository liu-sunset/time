package com.example.time

import android.os.Bundle
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.runtime.*
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.content.Context
import com.example.time.ui.screens.CountdownScreen
import com.example.time.ui.screens.TimePickerScreen
import com.example.time.ui.theme.TimeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        setContent {
            TimeTheme {
                var isCountdownStarted by remember { mutableStateOf(false) }
                var totalSeconds by remember { mutableStateOf(0L) }
                var isVibrationEnabled by remember { mutableStateOf(false) }

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
                        TimePickerScreen(
                            onStartClick = { seconds ->
                                totalSeconds = seconds
                                isCountdownStarted = true
                            }
                        )
                    } else {
                        CountdownScreen(
                            totalSeconds = totalSeconds,
                            onFinish = {
                                if (isVibrationEnabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        vibrator.vibrate(VibrationEffect.createWaveform(
                                            longArrayOf(0, 500, 500, 500),
                                            -1
                                        ))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibrator.vibrate(longArrayOf(0, 500, 500, 500), -1)
                                    }
                                }
                            },
                            onBack = { isCountdownStarted = false },
                            isVibrationEnabled = isVibrationEnabled,
                            onVibrationToggle = { enabled ->
                                isVibrationEnabled = enabled
                            }
                        )
                    }
                }
            }
        }
    }
}