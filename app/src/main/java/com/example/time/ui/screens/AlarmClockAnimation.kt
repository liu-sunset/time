package com.example.time.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.time.R

@Composable
fun AlarmClockAnimation(
    modifier: Modifier = Modifier
) {
    // 创建无限重复的动画
    val infiniteTransition = rememberInfiniteTransition(label = "alarmAnimation")
    
    // 闹钟摇动动画 - 增加幅度从5度到12度
    val clockRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f, 
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clockRotation"
    )
    
    // 使用图像资源
    Image(
        painter = painterResource(id = R.drawable.alarm_clock),
        contentDescription = "闹钟",
        modifier = modifier
            .size(200.dp)
            .graphicsLayer {
                rotationZ = clockRotation
            },
        colorFilter = ColorFilter.tint(Color.Gray)
    )
} 