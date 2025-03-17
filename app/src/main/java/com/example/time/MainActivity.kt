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
import android.os.IBinder
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.LaunchedEffect
import com.example.time.ui.screens.CountdownScreen
import com.example.time.ui.screens.TimePickerScreen
import com.example.time.ui.theme.TimeTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.time.utils.RingtonePlayer
import com.example.time.utils.rememberRingtonePlayer
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.Intent
import androidx.compose.runtime.collectAsState
import com.example.time.services.CountdownService

class MainActivity : ComponentActivity() {
    private lateinit var vibrator: Vibrator
    private var countdownService: CountdownService? = null
    private var isBound = false

    // 服务连接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val binder = iBinder as CountdownService.CountdownBinder
            countdownService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            countdownService = null
            isBound = false
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 绑定服务
        Intent(this, CountdownService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        
        // 优化1: 启用边缘到边缘显示，提前完成窗口设置
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 隐藏系统栏
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        
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
                    var isKeepScreenOn by remember { mutableStateOf(false) }
                    var isDarkMode by remember { mutableStateOf(false) }
                    var isStyleFixed by remember { mutableStateOf(false) }
                    
                    // 创建铃声播放器
                    val ringtonePlayer = rememberRingtonePlayer()
                    
                    // 可以添加一个铃声开关状态
                    var isAlarmSoundEnabled by remember { mutableStateOf(true) }
                    
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
                                    },
                                    isVibrationEnabled = isVibrationEnabled,
                                    onVibrationToggle = { enabled ->
                                        isVibrationEnabled = enabled
                                    },
                                    isKeepScreenOn = isKeepScreenOn,
                                    onKeepScreenOnToggle = { enabled ->
                                        isKeepScreenOn = enabled
                                    },
                                    isDarkMode = isDarkMode,
                                    onDarkModeToggle = { enabled ->
                                        isDarkMode = enabled
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
                                        // 返回时停止服务
                                        stopCountdownService()
                                    },
                                    isVibrationEnabled = isVibrationEnabled,
                                    onVibrationToggle = { enabled ->
                                        isVibrationEnabled = enabled
                                        // 更新服务中的振动设置
                                        val intent = Intent(this@MainActivity, CountdownService::class.java).apply {
                                            action = CountdownService.ACTION_UPDATE_SETTINGS
                                            putExtra(CountdownService.EXTRA_VIBRATION_ENABLED, enabled)
                                        }
                                        startService(intent)
                                    },
                                    vibrator = vibrator,
                                    isKeepScreenOn = isKeepScreenOn,
                                    onKeepScreenOnToggle = { enabled ->
                                        isKeepScreenOn = enabled
                                    },
                                    isDarkMode = isDarkMode,
                                    onDarkModeToggle = { enabled ->
                                        isDarkMode = enabled
                                    },
                                    isAlarmSoundEnabled = isAlarmSoundEnabled,
                                    ringtonePlayer = ringtonePlayer,
                                    isStyleFixed = isStyleFixed,
                                    onStyleFixedToggle = { enabled ->
                                        isStyleFixed = enabled
                                    },
                                    startCountdownService = { seconds, vibrationEnabled, alarmEnabled ->
                                        startCountdownService(seconds, vibrationEnabled, alarmEnabled)
                                    },
                                    stopCountdownService = {
                                        stopCountdownService()
                                    },
                                    serviceRemainingSeconds = if (isBound) countdownService?.remainingSeconds?.collectAsState()?.value else null,
                                    serviceCountdownFinished = if (isBound) countdownService?.isCountdownFinished?.collectAsState()?.value == true else false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        // 解绑服务
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        super.onDestroy()
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

    // 启动倒计时服务的辅助方法
    private fun startCountdownService(seconds: Long, isVibrationEnabled: Boolean, isAlarmEnabled: Boolean) {
        val intent = Intent(this, CountdownService::class.java).apply {
            action = CountdownService.ACTION_START_COUNTDOWN
            putExtra(CountdownService.EXTRA_COUNTDOWN_SECONDS, seconds)
            putExtra(CountdownService.EXTRA_VIBRATION_ENABLED, isVibrationEnabled)
            putExtra(CountdownService.EXTRA_ALARM_ENABLED, isAlarmEnabled)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    // 停止倒计时服务的辅助方法
    private fun stopCountdownService() {
        val intent = Intent(this, CountdownService::class.java).apply {
            action = CountdownService.ACTION_STOP_COUNTDOWN
        }
        startService(intent)
    }
}