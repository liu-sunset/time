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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.layout.systemBarsPadding

@Composable
fun TimePickerScreen(
    onStartClick: (Long) -> Unit,
    isVibrationEnabled: Boolean = false,
    onVibrationToggle: (Boolean) -> Unit = {},
    isKeepScreenOn: Boolean = false,
    onKeepScreenOnToggle: (Boolean) -> Unit = {},
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {}
) {
    var selectedHours by rememberSaveable { mutableStateOf(0) }
    var selectedMinutes by rememberSaveable { mutableStateOf(0) }
    var selectedSeconds by rememberSaveable { mutableStateOf(0) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("时间最小为0") }
    
    var isToolMenuExpanded by remember { mutableStateOf(false) }
    
    val configuration = LocalConfiguration.current
    val isLandscape = remember(configuration) { 
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE 
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 例如，在此处可以准备某些资源
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SideEffect {
        // 在组合成功后执行，例如预热某些计算
    }

    val view = LocalView.current
    DisposableEffect(isKeepScreenOn) {
        view.keepScreenOn = isKeepScreenOn
        onDispose {
            view.keepScreenOn = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) Color(0xFF212121) else Color(0xFFFAFAFA)
            )
    ) {
        // 只保留工具按钮
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            // 工具按钮
            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color.Black.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFE0E0E0),
                onClick = {
                    isToolMenuExpanded = true
                }
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "工具",
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 下拉菜单
            DropdownMenu(
                expanded = isToolMenuExpanded,
                onDismissRequest = { isToolMenuExpanded = false },
                modifier = Modifier
                    .background(Color.White)  // 将白色背景改为透明
            ) {
                // 震动选项 - 改为与工具按钮相同的样式
                Box(modifier = Modifier.padding(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Color.Black.copy(alpha = 0.25f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE0E0E0),  // 改为与工具按钮相同的颜色
                        onClick = {
                            onVibrationToggle(!isVibrationEnabled)
                            toastMessage = if (!isVibrationEnabled) "震动提醒已开启" else "震动提醒已关闭"
                            showToast = true
                            isToolMenuExpanded = false
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isVibrationEnabled) "震动关闭" else "震动开启",
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // 常亮选项 - 改为与工具按钮相同的样式
                Box(modifier = Modifier.padding(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Color.Black.copy(alpha = 0.25f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE0E0E0),  // 改为与工具按钮相同的颜色
                        onClick = {
                            onKeepScreenOnToggle(!isKeepScreenOn)
                            toastMessage = if (!isKeepScreenOn) "屏幕常亮已开启" else "屏幕常亮已关闭"
                            showToast = true
                            isToolMenuExpanded = false
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isKeepScreenOn) "常亮关闭" else "常亮开启",
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // 暗夜模式选项 - 改为与工具按钮相同的样式
                Box(modifier = Modifier.padding(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Color.Black.copy(alpha = 0.25f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE0E0E0),  // 改为与工具按钮相同的颜色
                        onClick = {
                            onDarkModeToggle(!isDarkMode)
                            toastMessage = if (!isDarkMode) "暗夜模式已开启" else "暗夜模式已关闭"
                            showToast = true
                            isToolMenuExpanded = false
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isDarkMode) "暗夜关闭" else "暗夜开启",
                                fontSize = 14.sp,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        
        if (!isLandscape) {
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
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
            NumberPicker(
                range = 0..23,
                onValueChange = onHoursChange,
                suffix = ""
            )
            
            Text(":", 
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 39.dp)
            )
            
            NumberPicker(
                range = 0..59,
                onValueChange = onMinutesChange,
                suffix = ""
            )
            
            Text(":", 
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 39.dp)
            )
            
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
            NumberPicker(
                range = 0..23,
                onValueChange = onHoursChange,
                suffix = ""
            )
            
            Text(":", 
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 39.dp)
            )
            
            NumberPicker(
                range = 0..59,
                onValueChange = onMinutesChange,
                suffix = ""
            )
            
            Text(":", 
                fontSize = 20.sp,
                modifier = Modifier.offset(y = 39.dp)
            )
            
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
        val density = LocalDensity.current
        var offsetX by remember { mutableFloatStateOf(0f) }
        var isDragging by remember { mutableStateOf(false) }
        var buttonWidth by remember { mutableStateOf(250.dp) }
        val maxSlideWidth = with(density) { buttonWidth.toPx() - 60.dp.toPx() }
        
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
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFFEEEEEE))
                .align(Alignment.Center)
                .onSizeChanged { size: IntSize ->
                    buttonWidth = with(density) { size.width.toDp() }
                }
        )
        
        Box(
            modifier = Modifier
                .width(with(density) { (60.dp + finalOffset.toDp()) })
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.Black)
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
    
    val itemHeight = 40.dp
    
    val extraItems = 1
    val totalItems = range.last - range.first + 1 + (extraItems * 2)
    
    val centerItemIndex = remember {
        derivedStateOf {
            val visibleInfo = listState.layoutInfo.visibleItemsInfo
            if (visibleInfo.isEmpty()) return@derivedStateOf 0
            
            val listCenter = listState.layoutInfo.viewportSize.height / 2
            
            val centerItem = visibleInfo.minByOrNull {
                Math.abs((it.offset + it.size / 2) - listCenter)
            } ?: return@derivedStateOf 0
            
            centerItem.index
        }
    }
    
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
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.Center),
                color = Color.Transparent,
                shape = MaterialTheme.shapes.small
            ) { }
        }

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(vertical = 40.dp)
        ) {
            items(totalItems) { index ->
                val adjustedIndex = index - extraItems
                
                val isExtraItem = adjustedIndex < range.first || adjustedIndex > range.last
                
                val value = when {
                    adjustedIndex < range.first -> range.first
                    adjustedIndex > range.last -> range.last
                    else -> adjustedIndex
                }
                
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isExtraItem) {
                        Text(
                            text = "${value.toString().padStart(2, '0')}$suffix",
                            fontSize = 42.sp,
                            fontWeight = if (index == centerItemIndex.value) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == centerItemIndex.value) 
                                Color.Black
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
} 