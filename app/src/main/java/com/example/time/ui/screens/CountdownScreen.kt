package com.example.time.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.time.R
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.zIndex
import com.example.time.utils.RingtonePlayer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

// 添加背景渐变色数据类
private data class GradientBackgroundColor(
    val name: String,
    val colors: List<Color>
)

// 添加卡片背景数据类
private data class CardBackground(
    val name: String,
    val color: Color,
    val hasTexture: Boolean = false
)

@Composable
fun CountdownScreen(
    totalSeconds: Long, 
    onFinish: () -> Unit, 
    onBack: () -> Unit,
    isVibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    vibrator: Vibrator,
    isKeepScreenOn: Boolean = false,
    onKeepScreenOnToggle: (Boolean) -> Unit = {},
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit = {},
    isAlarmSoundEnabled: Boolean = true,
    ringtonePlayer: RingtonePlayer,
    isStyleFixed: Boolean = false,
    onStyleFixedToggle: (Boolean) -> Unit = {},
    startCountdownService: (Long, Boolean, Boolean) -> Unit = { _, _, _ -> },
    stopCountdownService: () -> Unit = {},
    serviceRemainingSeconds: Long? = null,
    serviceCountdownFinished: Boolean = false
) {
    val view = LocalView.current
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    
    // 使用本地或服务提供的剩余时间
    var remainingSeconds by remember { 
        mutableStateOf(serviceRemainingSeconds ?: totalSeconds) 
    }
    var prevRemainingSeconds by remember { 
        mutableStateOf(serviceRemainingSeconds ?: totalSeconds) 
    }
    
    // 使用服务状态或本地状态
    var isCountdownFinished by remember { 
        mutableStateOf(serviceCountdownFinished) 
    }
    
    // 当进入界面时启动服务
    LaunchedEffect(Unit) {
        // 仅当剩余时间大于0时启动服务
        if (totalSeconds > 0) {
            startCountdownService(totalSeconds, isVibrationEnabled, isAlarmSoundEnabled)
        }
    }
    
    // 监听来自服务的剩余时间更新
    LaunchedEffect(serviceRemainingSeconds) {
        serviceRemainingSeconds?.let {
            prevRemainingSeconds = remainingSeconds
            remainingSeconds = it
        }
    }
    
    // 监听来自服务的倒计时完成状态
    LaunchedEffect(serviceCountdownFinished) {
        if (serviceCountdownFinished) {
            isCountdownFinished = true
            onFinish()
        }
    }
    
    // 检查倒计时时间是否为0
    LaunchedEffect(totalSeconds) {
        if (totalSeconds <= 0) {
            showToast = true
            toastMessage = "倒计时时间不能为0，请返回重新设置"
        }
    }
    
    // 在组件销毁时停止服务
    DisposableEffect(Unit) {
        onDispose {
            stopCountdownService()
        }
    }
    
    // 添加渐变背景颜色列表
    val gradientBackgrounds = listOf(
        GradientBackgroundColor(
            name = "晨雾森林",
            colors = listOf(Color(0xFFE6F4EA), Color(0xFFB8DFD8), Color(0xFF88C9B5))
        ),
        GradientBackgroundColor(
            name = "黄昏沙丘",
            colors = listOf(Color(0xFFFFEED9), Color(0xFFFDD2B5), Color(0xFFF7A88F))
        ),
        GradientBackgroundColor(
            name = "冰川呼吸",
            colors = listOf(Color(0xFFF0F5FF), Color(0xFFD6E4FF), Color(0xFFADC6FF))
        ),
        // 新增渐变背景色
        GradientBackgroundColor(
            name = "苔原迷雾",
            colors = listOf(Color(0xFFE0EBF5), Color(0xFFB7C9D6), Color(0xFF8FA6B7))
        ),
        GradientBackgroundColor(
            name = "宣纸渐变",
            colors = listOf(Color(0xFFF8F6F2), Color(0xFFE5E0D6), Color(0xFFD3CCC3))
        ),
        GradientBackgroundColor(
            name = "石墨波纹",
            colors = listOf(Color(0xFF404040), Color(0xFF606060), Color(0xFF808080))
        )
    )
    
    // 单色背景颜色列表（保留原有的）
    val solidBackgroundColors = listOf(
        Color.White,                    // 白色
        Color(0xFF6C8EB2),              // 雾霭蓝 RGB(108,142,178)
        Color(0xFFB4A6D5),              // 晨雾紫 RGB(180,166,213)
        Color(0xFFB76E79),              // 陶土红 RGB(183,110,121)
        Color(0xFFE9E1D6),              // 亚麻米 RGB(233,225,214)
        Color(0xFF71847D)               // 板岩绿 RGB(113,132,125)
    )
    
    // 背景索引和类型状态 - 确保初始背景为白色
    var colorIndex by remember { mutableStateOf(0) } // 索引0对应白色
    var useGradient by remember { mutableStateOf(false) } // 初始使用纯色背景
    var isFirstBackground by remember { mutableStateOf(true) } // 跟踪是否是第一次显示背景
    
    // 在两种背景系列之间随机切换，但确保第一次显示为白色
    LaunchedEffect(isDarkMode, isStyleFixed) {
        // 首次延迟较长时间，确保用户能看到白色背景
        if (isFirstBackground) {
            delay(10000) // 首次10秒后再切换
            isFirstBackground = false
        }
        
        while (true) {
            delay(1000*60*15) // 修改为5分钟切换一次
            if (!isDarkMode && !isStyleFixed) { // 只有在非暗黑模式且未固定样式时才切换
                // 获取当前颜色索引和类型
                val currentColorIndex = colorIndex
                val currentUseGradient = useGradient
                
                // 随机决定是否切换背景类型（渐变或纯色）
                val randomUseGradient = Random.nextBoolean()
                useGradient = randomUseGradient
                
                // 随机选择新的颜色索引，确保不与当前相同
                val maxIndex = if (randomUseGradient) 
                    gradientBackgrounds.size - 1 
                else 
                    solidBackgroundColors.size - 1
                    
                // 随机获取一个不同于当前索引的新索引
                var newIndex: Int
                do {
                    newIndex = Random.nextInt(0, maxIndex + 1)
                } while (newIndex == currentColorIndex && 
                         randomUseGradient == currentUseGradient)
                
                colorIndex = newIndex
            }
        }
    }
    
    // 添加常亮功能
    DisposableEffect(isKeepScreenOn) {
        // 获取Activity和Window
        val activity = view.context as? android.app.Activity
        activity?.window?.let { window ->
            // 直接在Window级别设置FLAG_KEEP_SCREEN_ON标志
            if (isKeepScreenOn) {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        
        // 同时也设置view的keepScreenOn属性作为双重保障
        view.keepScreenOn = isKeepScreenOn
        
        onDispose {
            // 清除窗口标志
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            view.keepScreenOn = false
        }
    }
    
    // 添加动画状态
    var isAlarmPlaying by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = remainingSeconds) {
        if (totalSeconds > 0 && remainingSeconds > 0) {
            delay(1000)
            prevRemainingSeconds = remainingSeconds
            remainingSeconds--
        } else if (totalSeconds > 0 && remainingSeconds <= 0) {
            isCountdownFinished = true  // 设置为已完成状态
            
            // 播放铃声
            if (isAlarmSoundEnabled) {
                isAlarmPlaying = true
                ringtonePlayer.playRingtone()
            }
            
            // 添加震动功能
            if (isVibrationEnabled) {
                startVibration(vibrator)
            }
            
            onFinish()
        }
    }

    // 在组件销毁时停止铃声
    DisposableEffect(Unit) {
        onDispose {
            if (isAlarmPlaying) {
                ringtonePlayer.stopRingtone()
            }
            // 确保组件销毁时停止震动
            if (isVibrationEnabled) {
                stopVibration(vibrator)
            }
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

    // 添加工具菜单状态
    var isToolMenuExpanded by remember { mutableStateOf(false) }
    
    // 添加卡片背景集合
    val cardBackgrounds = remember {
        listOf(
            CardBackground("白色", Color.White),
            CardBackground("淡黄纹理", Color(0xFFF7E9C3), true),
            CardBackground("浅蓝", Color(0xFFE6F4FA)),
            CardBackground("薄荷绿", Color(0xFFE0F2F1)),
            CardBackground("灰白", Color(0xFFF5F5F5)),
            CardBackground("米色", Color(0xFFF5F1E8))
        )
    }
    
    // 添加卡片背景索引状态
    var cardBackgroundIndex by remember { mutableStateOf(0) } // 初始为白色
    
    // 定期切换卡片背景色 (20分钟)
    LaunchedEffect(Unit, isStyleFixed) {
        // 首次等待较长时间，确保用户能看到白色背景
        delay(1000*60*20) // 20分钟
        
        while(true) {
            if (!isStyleFixed) { // 只有在未固定样式时才切换卡片背景
                // 切换到下一个背景
                cardBackgroundIndex = (cardBackgroundIndex + 1) % cardBackgrounds.size
            }
            delay(1000*60*20) // 20分钟切换一次
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (!isDarkMode && useGradient) {
                    // 使用渐变背景
                    val currentGradient = gradientBackgrounds[colorIndex]
                    Brush.verticalGradient(colors = currentGradient.colors)
                } else {
                    // 使用单色背景或暗色模式
                    val color = if (isDarkMode) {
                        Color(0xFF212121) // 暗黑模式保持原来的颜色
                    } else {
                        solidBackgroundColors[colorIndex]
                    }
                    Brush.verticalGradient(colors = listOf(color, color))
                },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
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
                color = if (isDarkMode) Color(0xFF212121) else Color(0xFFE0E0E0),
                onClick = {
                    if (isAlarmPlaying) {
                        ringtonePlayer.stopRingtone()
                        isAlarmPlaying = false
                    }
                    onBack()
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        fontSize = 24.sp,
                        color = if (isDarkMode) Color.White else Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // 添加工具按钮
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            // 添加按钮旋转动画
            val buttonRotation by animateFloatAsState(
                targetValue = if (isToolMenuExpanded) 225f else 0f,  // 增加旋转角度到225度
                animationSpec = tween(
                    durationMillis = 400, 
                    easing = FastOutSlowInEasing
                ),
                label = "buttonRotation"
            )
            
            // 添加按钮缩放动画
            val buttonScale by animateFloatAsState(
                targetValue = if (isToolMenuExpanded) 1.2f else 1.0f,  // 增加缩放至1.2倍
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing  // 修改为LinearEasing
                ),
                label = "buttonScale"
            )
            
            // 添加颜色变化动画
            val buttonColor by animateColorAsState(
                targetValue = if (isToolMenuExpanded) 
                    if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFC0C0C0)
                else 
                    if (isDarkMode) Color(0xFF212121) else Color(0xFFE0E0E0),
                animationSpec = tween<Color>(durationMillis = 300),  // 添加显式类型参数
                label = "buttonColor"
            )
            
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { 
                        rotationZ = buttonRotation
                        scaleX = buttonScale
                        scaleY = buttonScale
                    }
                    .shadow(
                        elevation = if (isToolMenuExpanded) 8.dp else 4.dp,  // 增加阴影深度变化
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = buttonColor,  // 使用动画颜色
                onClick = { isToolMenuExpanded = !isToolMenuExpanded }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "设置",
                        tint = if (isDarkMode) Color.White else Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // 工具菜单展开动画
        com.example.time.ui.screens.AnimatedToolMenu(
            isExpanded = isToolMenuExpanded,
            isVibrationEnabled = isVibrationEnabled,
            onVibrationToggle = onVibrationToggle,
            isKeepScreenOn = isKeepScreenOn,
            onKeepScreenOnToggle = onKeepScreenOnToggle,
            isDarkMode = isDarkMode,
            onDarkModeToggle = onDarkModeToggle,
            isStyleFixed = isStyleFixed,
            onStyleFixedToggle = onStyleFixedToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp)
                .zIndex(1f)
        )

        // 根据倒计时状态显示不同内容
        if (!isCountdownFinished) {
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
                        cardHeight = cardHeight,
                        currentBackground = cardBackgrounds[cardBackgroundIndex]
                    )
                    
                    // 分隔符
                    Text(
                        text = ":",
                        color = if (isDarkMode) Color.White else Color.Black,
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
                    cardHeight = cardHeight,
                    currentBackground = cardBackgrounds[cardBackgroundIndex]
                )
                
                // 分隔符
                Text(
                    text = ":",
                    color = if (isDarkMode) Color.White else Color.Black,
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
                    cardHeight = cardHeight,
                    currentBackground = cardBackgrounds[cardBackgroundIndex]
                )
            }
        } else {
            // 倒计时结束，显示闹钟动画
            AlarmClockAnimation(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(200.dp)
            )
        }
    }
    
    // 添加提示对话框
    if (showToast) {
        AlertDialog(
            onDismissRequest = { 
                if (totalSeconds <= 0) {
                    // 如果时间为0，关闭弹框后自动返回
                    onBack()
                } else {
                    showToast = false 
                }
            },
            title = { Text("提示") },
            text = { Text(toastMessage) },
            confirmButton = {
                TextButton(onClick = { 
                    if (totalSeconds <= 0) {
                        // 如果时间为0，点击确定后自动返回
                        onBack()
                    } else {
                        showToast = false
                    }
                }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun FlipTimeUnit(
    value: Int,
    prevValue: Int,
    digitSize: androidx.compose.ui.unit.TextUnit,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp,
    currentBackground: CardBackground
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
            cardHeight = cardHeight,
            currentBackground = currentBackground
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // 个位数字
        FlipDigit(
            digit = value % 10,
            prevDigit = prevValue % 10,
            digitSize = digitSize,
            cardWidth = cardWidth,
            cardHeight = cardHeight,
            currentBackground = currentBackground
        )
    }
}

@Composable
private fun FlipDigit(
    digit: Int,
    prevDigit: Int,
    digitSize: androidx.compose.ui.unit.TextUnit,
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp,
    currentBackground: CardBackground
) {
    val isFlipping = remember(digit, prevDigit) { digit != prevDigit }
    
    // 使用单一动画值
    val animationProgress = remember(digit, prevDigit) { Animatable(0f) }
    
    LaunchedEffect(digit, prevDigit) {
        if (isFlipping) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }
    
    // 使用单一卡片，通过Clip和动画值来控制显示
    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
    ) {
        // 上半部分背景卡片
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight / 2)
                .align(Alignment.TopCenter)
                .shadow(4.dp, RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp)),
            color = currentBackground.color
        ) {
            Box(contentAlignment = Alignment.Center) {
                // 如果有纹理，添加纹理层
                if (currentBackground.hasTexture) {
                    CardTexture(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // 静态上半部分数字
                if (animationProgress.value < 0.5f) {
                    Text(
                        text = prevDigit.toString(),
                        fontSize = digitSize,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = cardHeight / 4)
                    )
                } else {
                    Text(
                        text = digit.toString(),
                        fontSize = digitSize,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = cardHeight / 4)
                    )
                }
            }
        }
        
        // 下半部分背景卡片
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight / 2)
                .align(Alignment.BottomCenter)
                .shadow(4.dp, RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp))
                .clip(RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp)),
            color = currentBackground.color
        ) {
            Box(contentAlignment = Alignment.Center) {
                // 如果有纹理，添加纹理层
                if (currentBackground.hasTexture) {
                    CardTexture(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // 静态下半部分数字
                if (animationProgress.value < 0.5f) {
                    Text(
                        text = prevDigit.toString(),
                        fontSize = digitSize,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = -cardHeight / 4)
                    )
                } else {
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
        CustomDivider(
            color = Color(0xFFEEEEEE),
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // 翻转动画 - 使用单独的层
        if (isFlipping) {
            // 上半部分翻转 (0 -> 0.5)
            if (animationProgress.value < 0.5f) {
                val topFlipProgress = animationProgress.value * 2 // 0->1
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight / 2)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                        .graphicsLayer {
                            rotationX = -topFlipProgress * 90 // 0 -> -90
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        },
                    color = currentBackground.color
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // 如果有纹理，添加纹理层
                        if (currentBackground.hasTexture) {
                            CardTexture(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
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
            } else {
                // 下半部分翻转 (0.5 -> 1.0)
                val bottomFlipProgress = (animationProgress.value - 0.5f) * 2 // 0->1
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight / 2)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(0.dp, 0.dp, 8.dp, 8.dp))
                        .graphicsLayer {
                            rotationX = (1 - bottomFlipProgress) * 90 // 90 -> 0
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        },
                    color = currentBackground.color
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // 如果有纹理，添加纹理层
                        if (currentBackground.hasTexture) {
                            CardTexture(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
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
        }
    }
}

@Composable
fun CustomDivider(color: Color, thickness: androidx.compose.ui.unit.Dp, modifier: Modifier) {
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

private fun startVibration(vibrator: Vibrator) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(longArrayOf(0, 1000, 1000), 0)
    }
}

private fun stopVibration(vibrator: Vibrator) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.cancel()
    } else {
        @Suppress("DEPRECATION")
        vibrator.cancel()
    }
}

// 创建纹理组件
@Composable
fun CardTexture(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // 设置纹理绘制参数
        val canvasWidth = size.width
        val canvasHeight = size.height
        val lineSpacing = 5.dp.toPx()
        val lineThickness = 0.5.dp.toPx()
        
        // 绘制水平线纹理
        val paint = Paint().apply {
            color = Color(0x15000000) // 半透明黑色
            strokeWidth = lineThickness
            style = PaintingStyle.Stroke
        }
        
        // 水平线
        for (y in 0..canvasHeight.toInt() step lineSpacing.toInt()) {
            drawLine(
                color = Color(0x15000000),
                start = Offset(0f, y.toFloat()),
                end = Offset(canvasWidth, y.toFloat()),
                strokeWidth = lineThickness
            )
        }
        
        // 垂直线 - 较淡
        for (x in 0..canvasWidth.toInt() step lineSpacing.toInt()) {
            drawLine(
                color = Color(0x10000000),
                start = Offset(x.toFloat(), 0f),
                end = Offset(x.toFloat(), canvasHeight),
                strokeWidth = lineThickness
            )
        }
        
        // 添加随机的噪点效果
        val random = kotlin.random.Random
        val dotsCount = (canvasWidth * canvasHeight / 1000).toInt()
        val dotSize = 1.dp.toPx()
        
        repeat(dotsCount) {
            val x = random.nextFloat() * canvasWidth
            val y = random.nextFloat() * canvasHeight
            drawCircle(
                color = Color(0x0A000000),
                radius = dotSize / 2,
                center = Offset(x, y)
            )
        }
    }
}