package com.example.time.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import kotlin.math.abs
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

@Composable
fun TimePickerScreen(
    onStartClick: (Long) -> Unit
) {
    var selectedHours by remember { mutableStateOf(0) }
    var selectedMinutes by remember { mutableStateOf(0) }
    var selectedSeconds by remember { mutableStateOf(0) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("时间最小为0") }
    
    // 获取当前屏幕方向
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier.fillMaxSize()) {
        // 根据屏幕方向选择不同的布局
        if (!isLandscape) {
            // 竖屏布局
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PortraitTimePicker(
                    selectedHours, selectedMinutes, selectedSeconds,
                    onHoursChange = { selectedHours = it },
                    onMinutesChange = { selectedMinutes = it },
                    onSecondsChange = { selectedSeconds = it }
                )
                
                // 滑动按钮
                SlideToStartButton(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 32.dp),
                    selectedHours = selectedHours,
                    selectedMinutes = selectedMinutes,
                    selectedSeconds = selectedSeconds,
                    onStartClick = onStartClick,
                    onShowToast = { showToast = true }
                )
            }
        } else {
            // 横屏布局
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 左侧放时间选择器
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    LandscapeTimePicker(
                        selectedHours, selectedMinutes, selectedSeconds,
                        onHoursChange = { selectedHours = it },
                        onMinutesChange = { selectedMinutes = it },
                        onSecondsChange = { selectedSeconds = it }
                    )
                }
                
                // 右侧放滑动按钮
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    SlideToStartButton(
                        modifier = Modifier
                            .width(280.dp)
                            .height(60.dp),
                        selectedHours = selectedHours,
                        selectedMinutes = selectedMinutes,
                        selectedSeconds = selectedSeconds,
                        onStartClick = onStartClick,
                        onShowToast = { showToast = true }
                    )
                }
            }
        }
    }

    // 提示对话框
    if (showToast) {
        AlertDialog(
            onDismissRequest = { showToast = false },
            title = { Text("提示") },
            text = { Text(toastMessage) },
            confirmButton = {
                TextButton(onClick = { showToast = false }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun PortraitTimePicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .height(200.dp)
            .padding(horizontal = 32.dp),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-45).dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 小时选择器
            NumberPicker(
                range = 0..23,
                onValueChange = onHoursChange,
                suffix = ""
            )
            
            Text(":", 
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 39.dp)
            )
            
            // 分钟选择器
            NumberPicker(
                range = 0..59,
                onValueChange = onMinutesChange,
                suffix = ""
            )
            
            Text(":", 
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 39.dp)
            )
            
            // 秒选择器
            NumberPicker(
                range = 0..59,
                onValueChange = onSecondsChange,
                suffix = ""
            )
        }
    }
}

@Composable
private fun LandscapeTimePicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .height(180.dp)
            .width(320.dp),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-40).dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 小时选择器
            NumberPicker(
                range = 0..23,
                onValueChange = onHoursChange,
                suffix = ""
            )
            
            Text(":", 
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 39.dp)
            )
            
            // 分钟选择器
            NumberPicker(
                range = 0..59,
                onValueChange = onMinutesChange,
                suffix = ""
            )
            
            Text(":", 
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 39.dp)
            )
            
            // 秒选择器
            NumberPicker(
                range = 0..59,
                onValueChange = onSecondsChange,
                suffix = ""
            )
        }
    }
}

@Composable
private fun SlideToStartButton(
    modifier: Modifier = Modifier,
    selectedHours: Int,
    selectedMinutes: Int,
    selectedSeconds: Int,
    onStartClick: (Long) -> Unit,
    onShowToast: () -> Unit
) {
    Box(modifier = modifier) {
        // 使用本地密度转换器
        val density = LocalDensity.current
        // 跟踪滑动位置
        var offsetX by remember { mutableFloatStateOf(0f) }
        // 是否正在拖动
        var isDragging by remember { mutableStateOf(false) }
        // 使用onSizeChanged获取实际宽度
        var buttonWidth by remember { mutableStateOf(250.dp) }
        val maxSlideWidth = with(density) { buttonWidth.toPx() - 60.dp.toPx() }
        
        // 动画过渡逻辑
        val animatedOffset by animateFloatAsState(
            targetValue = offsetX,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = 0.1f
            ),
            label = "slideAnimation"
        )
        
        val finalOffset = animatedOffset
        
        // 创建黑色渐变背景
        val gradientBackground = Brush.horizontalGradient(
            colors = listOf(
                Color.Black,
                Color(0xFF333333),
                Color(0xFF555555)
            )
        )
        
        // 滑动轨道背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFFEEEEEE))
                .align(Alignment.Center)
                .onSizeChanged { size: IntSize ->
                    // 更新按钮实际宽度
                    buttonWidth = with(density) { size.width.toDp() }
                }
        )
        
        // 滑块
        Box(
            modifier = Modifier
                .width(with(density) { (60.dp + finalOffset.toDp()) })
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(gradientBackground)
                .shadow(4.dp, RoundedCornerShape(30.dp))
                .align(Alignment.CenterStart)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val dampingFactor = 1f - (offsetX / maxSlideWidth) * 0.05f
                        offsetX = (offsetX + delta * dampingFactor).coerceIn(0f, maxSlideWidth)
                    },
                    onDragStarted = { isDragging = true },
                    onDragStopped = {
                        if (offsetX >= maxSlideWidth) {
                            val totalSeconds = (selectedHours * 3600L) + 
                                     (selectedMinutes * 60L) + 
                                     selectedSeconds
                        
                            if (totalSeconds > 0) {
                                isDragging = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(50)
                                    offsetX = 0f
                                    delay(100)
                                    onStartClick(totalSeconds)
                                }
                            } else {
                                onShowToast()
                                isDragging = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(50)
                                    offsetX = 0f
                                }
                            }
                        } else {
                            isDragging = false
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(30)
                                offsetX = 0f
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
           
            
            // 缩放效果
            val scale = 1f + (finalOffset / maxSlideWidth) * 0.05f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

@Composable
private fun NumberPicker(
    range: IntRange,
    onValueChange: (Int) -> Unit,
    suffix: String
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = range.first
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)
    
    // 设置滚动停止位置为整数倍的项高度
    val itemHeight = 40.dp
    
    // 添加额外的项使选择器可以滚动到列表开头和结尾
    val extraItems = 1
    val totalItems = range.last - range.first + 1 + (extraItems * 2)
    
    // 使用derivedStateOf找出中间可见项
    val centerItemIndex = remember {
        derivedStateOf {
            val visibleInfo = listState.layoutInfo.visibleItemsInfo
            if (visibleInfo.isEmpty()) return@derivedStateOf 0
            
            // 找出中间位置坐标
            val listCenter = listState.layoutInfo.viewportSize.height / 2
            
            // 找到最接近中心的项
            val centerItem = visibleInfo.minByOrNull {
                Math.abs((it.offset + it.size / 2) - listCenter)
            } ?: return@derivedStateOf 0
            
            // 返回这个项在列表中的索引
            centerItem.index
        }
    }
    
    // 当中间项变化时更新选中的值
    LaunchedEffect(centerItemIndex.value) {
        val adjustedIndex = centerItemIndex.value - extraItems
        val selectedValue = when {
            adjustedIndex < range.first -> range.first
            adjustedIndex > range.last -> range.last
            else -> adjustedIndex
        }
        
        onValueChange(selectedValue)
    }

    Box(
        modifier = Modifier
            .height(120.dp)
            .width(60.dp),
        contentAlignment = Alignment.Center
    ) {
        // 这里的布局顺序很重要 - 先绘制背景
        
        // 绘制只保留选中区域背景
        Box(modifier = Modifier.fillMaxSize()) {
            // 选中区域背景 - 放在中间（修改背景为透明，让背景色直接显示）
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.Center),
                color = Color.Transparent,  // 改为透明色，这样会显示父级背景
                shape = MaterialTheme.shapes.small
            ) { }
        }

        // 然后绘制LazyColumn内容
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
            // 关键是调整内边距让中间项对齐到中央
            contentPadding = PaddingValues(vertical = 40.dp)
        ) {
            items(totalItems) { index ->
                // 调整索引以考虑额外的项
                val adjustedIndex = index - extraItems
                
                // 判断是否为额外项
                val isExtraItem = adjustedIndex < range.first || adjustedIndex > range.last
                
                // 确定要显示的值
                val value = when {
                    adjustedIndex < range.first -> range.first
                    adjustedIndex > range.last -> range.last
                    else -> adjustedIndex
                }
                
                // 固定高度确保对齐
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // 只在非额外项上显示文本
                    if (!isExtraItem) {
                        Text(
                            text = "${value.toString().padStart(2, '0')}$suffix",
                            fontSize = 42.sp,
                            fontWeight = if (index == centerItemIndex.value) FontWeight.Bold else FontWeight.Normal,
                            // 修改选中项的颜色为黑色，而不是使用主题色
                            color = if (index == centerItemIndex.value) 
                                Color.Black  // 改为黑色
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        // 对于额外项，不显示任何内容
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
} 