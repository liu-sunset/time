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

@Composable
fun TimePickerScreen(onStartClick: (Long) -> Unit) {
    var selectedHours by remember { mutableStateOf(0) }
    var selectedMinutes by remember { mutableStateOf(0) }
    var selectedSeconds by remember { mutableStateOf(0) }
    var showToast by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "设置倒计时",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Surface(
            modifier = Modifier
                .height(200.dp)
                .padding(horizontal = 32.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 小时选择器
                NumberPicker(
                    range = 0..23,
                    onValueChange = { selectedHours = it },
                    suffix = "时"
                )
                
                Text(":", fontSize = 20.sp)
                
                // 分钟选择器
                NumberPicker(
                    range = 0..59,
                    onValueChange = { selectedMinutes = it },
                    suffix = "分"
                )
                
                Text(":", fontSize = 20.sp)
                
                // 秒选择器
                NumberPicker(
                    range = 0..59,
                    onValueChange = { selectedSeconds = it },
                    suffix = "秒"
                )
            }
        }

        Button(
            onClick = {
                val totalSeconds = (selectedHours * 3600L) + 
                                 (selectedMinutes * 60L) + 
                                 selectedSeconds
                if (totalSeconds > 0) {
                    onStartClick(totalSeconds)
                } else {
                    // 显示提示
                    showToast = true
                }
            },
            modifier = Modifier.padding(top = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                "开始倒计时",
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                fontSize = 18.sp
            )
        }
    }

    // 添加提示对话框
    if (showToast) {
        AlertDialog(
            onDismissRequest = { showToast = false },
            title = { Text("提示") },
            text = { Text("请设置大于0的倒计时时间") },
            confirmButton = {
                TextButton(onClick = { showToast = false }) {
                    Text("确定")
                }
            }
        )
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
        // 先添加选中项指示器，确保它们在列表后面
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
            shape = MaterialTheme.shapes.small
        ) { }
        
        // 上方分隔线
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        // 下方分隔线
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 20.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
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
                        .height(40.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // 只在非额外项上显示文本
                    if (!isExtraItem) {
                        Text(
                            text = "${value.toString().padStart(2, '0')}$suffix",
                            fontSize = 20.sp,
                            fontWeight = if (index == centerItemIndex.value) FontWeight.Bold else FontWeight.Normal,
                            // 根据是否为中间选定项调整透明度
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (index == centerItemIndex.value) 1f else 0.6f
                            )
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