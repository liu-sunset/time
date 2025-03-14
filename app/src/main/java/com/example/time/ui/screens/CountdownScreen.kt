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

@Composable
fun CountdownScreen(totalSeconds: Long, onFinish: () -> Unit) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)),
        contentAlignment = Alignment.Center
    ) {
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
        val cardHeight = if (hasHours) 150.dp else 180.dp
        
        // 翻页时钟组件
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            if (hasHours) {
                // 显示小时
                FlipTimeUnit(
                    value = hours.toInt(),
                    prevValue = prevHours.toInt(),
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
                value = minutes.toInt(),
                prevValue = prevMinutes.toInt(),
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
                value = seconds.toInt(),
                prevValue = prevSeconds.toInt(),
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
    
    // 动画进度
    val flipRotation = remember { Animatable(0f) }
    
    // 当数字变化时启动动画
    LaunchedEffect(digit, prevDigit) {
        if (isFlipping) {
            flipRotation.snapTo(0f)
            flipRotation.animateTo(
                targetValue = 180f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
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
        // 上半部分（静态或翻转）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight / 2)
                .align(Alignment.TopCenter)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp),
                    clip = true
                )
                .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                .graphicsLayer {
                    // 上半部分在动画后半段翻转
                    if (flipRotation.value > 90f) {
                        // 翻转完成，显示新数字
                        rotationX = 0f
                    } else {
                        // 正在翻转，显示旧数字
                        rotationX = 0f
                    }
                    // 翻转时增加阴影深度
                    shadowElevation = if (flipRotation.value in 1f..179f) 8f else 4f
                },
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
        
        // 翻转的上半部分（动画中）
        if (isFlipping) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight / 2)
                    .align(Alignment.TopCenter)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp),
                        clip = true
                    )
                    .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                    .graphicsLayer {
                        // 上半部分翻转
                        rotationX = if (flipRotation.value <= 90f) -flipRotation.value else -180f
                        // 在90度时切换数字
                        cameraDistance = 8f * density
                        // 设置变换原点为底部
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
        
        // 下半部分（静态）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight / 2)
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 4.dp,
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
        
        // 翻转的下半部分（动画中）
        if (isFlipping && flipRotation.value > 90f) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight / 2)
                    .align(Alignment.BottomCenter)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp),
                        clip = true
                    )
                    .clip(RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp))
                    .graphicsLayer {
                        // 当上半部分翻转超过90度后，下半部分开始翻转
                        rotationX = if (flipRotation.value > 90f) 180f - flipRotation.value else 0f
                        cameraDistance = 8f * density
                        // 设置变换原点为顶部
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