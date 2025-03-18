package com.example.time.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import com.example.time.MainActivity
import com.example.time.R
import com.example.time.utils.RingtonePlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CountdownService : Service() {
    private val binder = CountdownBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var countdownJob: Job? = null
    
    private lateinit var vibrator: Vibrator
    private lateinit var ringtonePlayer: RingtonePlayer
    
    // 倒计时状态
    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds
    
    // 配置状态
    private var isVibrationEnabled = false
    private var isAlarmSoundEnabled = true
    
    // 服务状态
    private val _isCountdownFinished = MutableStateFlow(false)
    val isCountdownFinished: StateFlow<Boolean> = _isCountdownFinished
    
    // 添加WakeLock变量
    private lateinit var wakeLock: WakeLock
    
    // 在已有变量之后添加
    private var isWakeLockHeld = false
    
    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        ringtonePlayer = RingtonePlayer(this)
        
        // 初始化WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "CountdownService::WakeLock"
        )
        
        // 创建通知渠道
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 从Intent中获取启动参数
        if (intent != null) {
            val action = intent.action
            
            when (action) {
                ACTION_START_COUNTDOWN -> {
                    val seconds = intent.getLongExtra(EXTRA_COUNTDOWN_SECONDS, 0)
                    val vibrationEnabled = intent.getBooleanExtra(EXTRA_VIBRATION_ENABLED, false)
                    val alarmEnabled = intent.getBooleanExtra(EXTRA_ALARM_ENABLED, true)
                    
                    startCountdown(seconds, vibrationEnabled, alarmEnabled)
                }
                ACTION_STOP_COUNTDOWN -> {
                    stopCountdown()
                    stopSelf()
                }
                ACTION_UPDATE_SETTINGS -> {
                    isVibrationEnabled = intent.getBooleanExtra(EXTRA_VIBRATION_ENABLED, isVibrationEnabled)
                    isAlarmSoundEnabled = intent.getBooleanExtra(EXTRA_ALARM_ENABLED, isAlarmSoundEnabled)
                    updateNotification()
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        // 确保释放WakeLock
        if (isWakeLockHeld && wakeLock.isHeld) {
            try {
                wakeLock.release()
                isWakeLockHeld = false
            } catch (e: Exception) {
                // 处理异常
            }
        }
        
        stopCountdown()
        super.onDestroy()
    }
    
    private fun startCountdown(seconds: Long, vibrationEnabled: Boolean, alarmEnabled: Boolean) {
        try {
            _isCountdownFinished.value = false
            isVibrationEnabled = vibrationEnabled
            isAlarmSoundEnabled = alarmEnabled
            _remainingSeconds.value = seconds
            
            // 获取并持有WakeLock，确保在锁屏时CPU继续运行
            if (!isWakeLockHeld && seconds > 0) {
                try {
                    wakeLock.acquire(seconds * 1000 + 60000) // 倒计时时间 + 额外1分钟
                    isWakeLockHeld = true
                } catch (e: Exception) {
                    // 处理WakeLock获取失败的情况
                    isWakeLockHeld = false
                    // 记录日志但继续执行
                }
            }
            
            // 启动为前台服务 - 添加错误处理
            try {
                startForeground(NOTIFICATION_ID, createNotification())
            } catch (e: Exception) {
                // 处理前台服务启动失败
                // 可以尝试使用不同的通知配置再次尝试
                try {
                    val simpleNotification = createSimpleNotification()
                    startForeground(NOTIFICATION_ID, simpleNotification)
                } catch (e: Exception) {
                    // 如果仍然失败，至少让服务继续运行
                }
            }
            
            // 启动倒计时
            countdownJob?.cancel()
            countdownJob = serviceScope.launch {
                while (_remainingSeconds.value > 0) {
                    delay(1000)
                    _remainingSeconds.value--
                    try {
                        updateNotification()
                    } catch (e: Exception) {
                        // 处理通知更新失败
                    }
                }
                
                // 倒计时结束
                _isCountdownFinished.value = true
                
                // 触发震动
                if (isVibrationEnabled) {
                    try {
                        startVibration()
                    } catch (e: Exception) {
                        // 处理震动失败
                    }
                }
                
                // 播放铃声
                if (isAlarmSoundEnabled) {
                    try {
                        ringtonePlayer.playRingtone()
                    } catch (e: Exception) {
                        // 处理铃声播放失败
                    }
                }
                
                // 更新通知
                try {
                    updateNotification(isFinished = true)
                } catch (e: Exception) {
                    // 处理通知更新失败
                }
            }
        } catch (e: Exception) {
            // 处理整体启动失败
            // 至少让服务保持运行状态
        }
    }
    
    private fun stopCountdown() {
        countdownJob?.cancel()
        stopVibration()
        ringtonePlayer.stopRingtone()
        
        // 释放WakeLock
        if (isWakeLockHeld) {
            try {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                isWakeLockHeld = false
            } catch (e: Exception) {
                // 处理可能的异常
            }
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
    
    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "倒计时通道",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "显示倒计时进度"
                }
                
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            // 处理通知渠道创建失败
        }
    }
    
    private fun createNotification(isFinished: Boolean = false): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val timeString = formatTime(_remainingSeconds.value)
        val contentTitle = if (isFinished) "倒计时已结束！" else "倒计时进行中"
        val contentText = if (isFinished) "点击查看" else "剩余时间: $timeString"
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true) // 确保通知本身不发出声音
            .build()
    }
    
    private fun updateNotification(isFinished: Boolean = false) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(isFinished))
    }
    
    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }
    
    private fun startVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 1000, 1000), 0)
        }
    }
    
    private fun stopVibration() {
        vibrator.cancel()
    }
    
    private fun createSimpleNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("倒计时")
            .setContentText("倒计时运行中")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }
    
    inner class CountdownBinder : Binder() {
        fun getService(): CountdownService = this@CountdownService
    }
    
    companion object {
        private const val CHANNEL_ID = "countdown_channel"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_COUNTDOWN = "com.example.time.action.START_COUNTDOWN"
        const val ACTION_STOP_COUNTDOWN = "com.example.time.action.STOP_COUNTDOWN"
        const val ACTION_UPDATE_SETTINGS = "com.example.time.action.UPDATE_SETTINGS"
        
        const val EXTRA_COUNTDOWN_SECONDS = "extra_countdown_seconds"
        const val EXTRA_VIBRATION_ENABLED = "extra_vibration_enabled"
        const val EXTRA_ALARM_ENABLED = "extra_alarm_enabled"
    }
} 