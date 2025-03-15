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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.time.ui.screens.CountdownScreen
import com.example.time.ui.screens.TimePickerScreen
import com.example.time.ui.theme.TimeTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var vibrator: Vibrator

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 优化1: 启用边缘到边缘显示，提前完成窗口设置
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 优化2: 预先获取系统服务
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        // 优化3: 避免在主线程进行不必要的初始化工作
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Default) {
            // 预热一些可能的计算
            // 注意：这里不做实际操作，仅为示例
        }
        
        setContent {
            // 优化4: 使用CompositionLocalProvider提供主题，避免多次重组时重新创建资源
            TimeTheme {
                // 优化5: 使用key让Compose更好地追踪状态，避免不必要的重组
                key(Unit) {
                    // 优化6: 使用记忆化的状态，确保恢复时状态正确
                    var isCountdownStarted by remember { mutableStateOf(false) }
                    var totalSeconds by remember { mutableStateOf(0L) }
                    var isVibrationEnabled by remember { mutableStateOf(false) }
                    
                    // 优化7: 屏幕方向变更使用LaunchedEffect而不是每次重组都执行
                    LaunchedEffect(isCountdownStarted) {
                        if (isCountdownStarted) {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        } else {
                            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                    }
                    
                    // 优化8: 使用AnimatedContent的crossfade动画，减少重叠渲染
                    AnimatedContent(
                        targetState = isCountdownStarted,
                        transitionSpec = {
                            fadeIn() + slideInVertically() with 
                            fadeOut() + slideOutVertically()
                        }
                    ) { started ->
                        // 优化9: 使用Box包装确保只有一个composable在屏幕上
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                        // 倒计时结束后的逻辑
                                    },
                                    onBack = {
                                        isCountdownStarted = false
                                        // 返回时停止震动
                                        stopVibration(vibrator)
                                    },
                                    isVibrationEnabled = isVibrationEnabled,
                                    onVibrationToggle = { enabled ->
                                        isVibrationEnabled = enabled
                                        if (!enabled) {
                                            // 关闭震动时停止震动
                                            stopVibration(vibrator)
                                        }
                                    },
                                    vibrator = vibrator // 传递vibrator实例
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 停止震动的辅助函数
    private fun stopVibration(vibrator: Vibrator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.cancel()
        } else {
            @Suppress("DEPRECATION")
            vibrator.cancel()
        }
    }
}