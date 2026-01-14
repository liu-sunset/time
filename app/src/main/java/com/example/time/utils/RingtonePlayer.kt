package com.example.time.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 负责播放系统闹钟铃声的工具类
 */
class RingtonePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    
    // 获取系统默认闹钟铃声
    private val defaultAlarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    
    // 如果没有闹钟铃声，则使用通知铃声
    private val fallbackUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    
    fun playRingtone() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                val soundUri = if (defaultAlarmUri == Uri.EMPTY) fallbackUri else defaultAlarmUri
                
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        setAudioStreamType(AudioManager.STREAM_ALARM)
                    }
                    
                    setDataSource(context, soundUri)
                    setLooping(true)
                    setOnErrorListener { _, _, _ ->
                        stopRingtone()
                        true
                    }
                    prepare()
                    start()
                } catch (e: Exception) {
                    e.printStackTrace()
                    stopRingtone()
                }
            }
        } else {
            try {
                if (!mediaPlayer!!.isPlaying) {
                    mediaPlayer?.start()
                }
            } catch (e: Exception) {
                stopRingtone()
            }
        }
    }
    
    fun stopRingtone() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        mediaPlayer = null
    }
    
    // 确保资源释放
    fun release() {
        stopRingtone()
    }
}

@Composable
fun rememberRingtonePlayer(): RingtonePlayer {
    val context = LocalContext.current
    val ringtonePlayer = remember { RingtonePlayer(context) }
    
    // 当组件销毁时释放资源
    DisposableEffect(Unit) {
        onDispose {
            ringtonePlayer.release()
        }
    }
    
    return ringtonePlayer
} 