package com.example.time.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.LaunchedEffect
import android.view.View
import androidx.compose.ui.platform.LocalView

@Composable
fun CountdownScreen(totalSeconds: Long, onFinish: () -> Unit, onBack: () -> Unit) {
    val view = LocalView.current
    
    LaunchedEffect(key1 = Unit) {
        // 修改为使用 from 方法获取 WindowInsetsControllerCompat 实例
        val activity = view.context as android.app.Activity
        val controller = WindowInsetsControllerCompat(activity.window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
    var remainingSeconds by remember { mutableStateOf(totalSeconds) }
    var prevRemainingSeconds by remember { mutableStateOf(totalSeconds) }
    
    LaunchedEffect(key1 = remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(1000)
            prevRemainingSeconds = remainingSeconds
            remainingSeconds--
        } else {
            onFinish()
        }
    }

    // 使用derivedStateOf来减少重组计算
    val timeValues = remember(remainingSeconds, prevRemainingSeconds) {
        derivedStateOf {
            // 计算时分秒
            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60
            
            // 计算上一秒的时分秒
            val prevHours = prevRemainingSeconds / 3600
            val prevMinutes = (prevRemainingSeconds % 3600) / 60
            val prevSeconds = prevRemainingSeconds % 60
            
            // 根据是否有小时数调整数字大小
            val hasHours = hours > 0 || prevHours > 0
            val digitSize = if (hasHours) 80.sp else 100.sp
            val cardWidth = if (hasHours) 110.dp else 140.dp
            val cardHeight = if (hasHours) 170.dp else 190.dp
            
            TimeDisplayValues(
                hours.toInt(), minutes.toInt(), seconds.toInt(),
                prevHours.toInt(), prevMinutes.toInt(), prevSeconds.toInt(),
                hasHours, digitSize, cardWidth, cardHeight
            )
        }
    }.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // 添加返回按钮
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFE0E0E0),
                onClick = onBack
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        fontSize = 24.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 原有的倒计时显示内容
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            // 使用计算好的值
            val (hours, minutes, seconds, prevHours, prevMinutes, prevSeconds, hasHours, digitSize, cardWidth, cardHeight) = timeValues
            
            // 翻页时钟组件
            if (hasHours) {
                // 显示小时
                FlipTimeUnit(
                    value = hours,
                    prevValue = prevHours,
                    digitSize = digitSize,
                    cardWidth = cardWidth,
                    cardHeight = cardHeight
                )
                
                // 分隔符
                Text(
                    text = ":",
                    color = Color.Black,
                    fontSize = if (hasHours) 60.sp else 80.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            
            // 显示分钟
            FlipTimeUnit(
                value = minutes,
                prevValue = prevMinutes,
                digitSize = digitSize,
                cardWidth = cardWidth,
                cardHeight = cardHeight
            )
            
            // 分隔符
            Text(
                text = ":",
                color = Color.Black,
                fontSize = if (hasHours) 60.sp else 80.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // 显示秒钟
            FlipTimeUnit(
                value = seconds,
                prevValue = prevSeconds,
                digitSize = digitSize,
                cardWidth = cardWidth,
                cardHeight = cardHeight
            )
        }
    }
}

@Composable
fun FlipTimeUnit(
    value: Int,
    prevValue: Int,
    digitSize: androidx.compose.ui.unit.TextUnit,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 十位数字
        FlipDigit(
            digit = value / 10,
            prevDigit = prevValue / 10,
            digitSize = digitSize,
            cardWidth = cardWidth,
            cardHeight = cardHeight
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // 个位数字
        FlipDigit(
            digit = value % 10,
            prevDigit = prevValue % 10,
            digitSize = digitSize,
            cardWidth = cardWidth,
            cardHeight = cardHeight
        )
    }
}

@Composable
fun FlipDigit(
    digit: Int,
    prevDigit: Int,
    digitSize: androidx.compose.ui.unit.TextUnit,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp
) {
    // 检测数字是否变化
    val isFlipping = remember(digit, prevDigit) { digit != prevDigit }
    
    // 动画进度 - 使用更高的初始值避免从零开始的卡顿
    val flipRotation = remember { Animatable(0f) }
    
    // 当数字变化时启动动画 - 优化动画速度和曲线
    LaunchedEffect(digit, prevDigit) {
        if (isFlipping) {
            flipRotation.snapTo(0f)
            flipRotation.animateTo(
                targetValue = 180f,
                animationSpec = tween(
                    durationMillis = 300,  // 减少动画时间从400ms到300ms
                    easing = LinearOutSlowInEasing  // 使用更流畅的缓动函数
                )
            )
        }
    }
    
    // 翻页数字容器
    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
    ) {
        // 上半部分（静态）- 减少不必要的图层变换
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight / 2)
                .align(Alignment.TopCenter)
                .shadow(
                    elevation = if (isFlipping && flipRotation.value in 1f..179f) 6.dp else 3.dp,  // 减小阴影
                    shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp),
                    clip = true
                )
                .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)),
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (flipRotation.value > 90f) digit.toString() else prevDigit.toString(),
                    fontSize = digitSize,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = cardHeight / 4)
                )
            }
        }
        
        // 翻转的上半部分（动画中）- 优化变换属性
        if (isFlipping) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight / 2)
                    .align(Alignment.TopCenter)
                    .shadow(
                        elevation = 6.dp,  // 固定阴影深度，减少动态计算
                        shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp),
                        clip = true
                    )
                    .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                    .graphicsLayer {
                        // 优化3D变换计算
                        rotationX = if (flipRotation.value <= 90f) -flipRotation.value else -180f
                        cameraDistance = 12f * density  // 增加相机距离使动画更平滑
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1.0f)
                    },
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = prevDigit.toString(),
                        fontSize = digitSize,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = cardHeight / 4)
                    )
                }
            }
        }
        
        // 下半部分（静态）- 简化属性
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight / 2)
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = if (isFlipping && flipRotation.value in 1f..179f) 6.dp else 3.dp,  // 减小阴影
                    shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp),
                    clip = true
                )
                .clip(RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)),
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (flipRotation.value > 90f) digit.toString() else prevDigit.toString(),
                    fontSize = digitSize,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = -cardHeight / 4)
                )
            }
        }
        
        // 翻转的下半部分（只在必要时显示）
        if (isFlipping && flipRotation.value > 90f) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight / 2)
                    .align(Alignment.BottomCenter)
                    .shadow(
                        elevation = 6.dp,  // 固定阴影深度
                        shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp),
                        clip = true
                    )
                    .clip(RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp))
                    .graphicsLayer {
                        rotationX = 180f - flipRotation.value
                        cameraDistance = 12f * density  // 增加相机距离
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.0f)
                    },
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = digit.toString(),
                        fontSize = digitSize,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = -cardHeight / 4)
                    )
                }
            }
        }
        
        // 中间分隔线
        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun Divider(color: Color, thickness: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

// 添加数据类来保存计算结果
private data class TimeDisplayValues(
    val hours: Int, val minutes: Int, val seconds: Int,
    val prevHours: Int, val prevMinutes: Int, val prevSeconds: Int,
    val hasHours: Boolean, val digitSize: TextUnit, 
    val cardWidth: Dp, val cardHeight: Dp
)