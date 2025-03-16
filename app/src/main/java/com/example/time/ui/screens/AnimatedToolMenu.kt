package com.example.time.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.DarkMode

// 添加一个自定义的缓动函数用于更平滑的滑动效果
val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

// 添加弹跳效果的缓动函数
val EaseOutBounce = Easing { fraction ->
    val n1 = 7.5625f
    val d1 = 2.75f
    var x = fraction
    
    when {
        x < 1f / d1 -> {
            n1 * x * x
        }
        x < 2f / d1 -> {
            x -= 1.5f / d1
            n1 * x * x + 0.75f
        }
        x < 2.5f / d1 -> {
            x -= 2.25f / d1
            n1 * x * x + 0.9375f
        }
        else -> {
            x -= 2.625f / d1
            n1 * x * x + 0.984375f
        }
    }
}

@Composable
fun CustomMenuDivider(color: Color, thickness: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

@Composable
fun AnimatedToolMenu(
    isExpanded: Boolean,
    isVibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit,
    isKeepScreenOn: Boolean,
    onKeepScreenOnToggle: (Boolean) -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 创建包含图标、标题、状态和回调的菜单项数据
    val menuItems = listOf(
        MenuItemData(
            icon = Icons.Filled.Phone,
            title = "震动",
            statusText = if (isVibrationEnabled) "已开启" else "已关闭",
            isEnabled = isVibrationEnabled,
            onClick = { onVibrationToggle(!isVibrationEnabled) }
        ),
        MenuItemData(
            icon = Icons.Filled.Lightbulb,
            title = "常亮",
            statusText = if (isKeepScreenOn) "已开启" else "已关闭",
            isEnabled = isKeepScreenOn,
            onClick = { onKeepScreenOnToggle(!isKeepScreenOn) }
        ),
        MenuItemData(
            icon = Icons.Filled.DarkMode,
            title = "暗黑模式",
            statusText = if (isDarkMode) "已开启" else "已关闭",
            isEnabled = isDarkMode,
            onClick = { onDarkModeToggle(!isDarkMode) }
        )
    )
    
    // 添加动画
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.8f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "scaleAnimation"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "alphaAnimation"
    )
    
    Box(modifier = modifier) {
        if (alpha > 0) {
            Surface(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF2D2D2D) else Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .width(220.dp)  // 增加宽度以适应图标和状态文字
                ) {
                    menuItems.forEachIndexed { index, menuItem ->
                        // 为每个菜单项添加延迟进入动画
                        val itemScale by animateFloatAsState(
                            targetValue = if (isExpanded) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 200,
                                delayMillis = 50 * index,
                                easing = FastOutSlowInEasing
                            ),
                            label = "itemScaleAnimation"
                        )
                        
                        val itemAlpha by animateFloatAsState(
                            targetValue = if (isExpanded) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 200,
                                delayMillis = 50 * index,
                                easing = FastOutSlowInEasing
                            ),
                            label = "itemAlphaAnimation"
                        )
                        
                        // 添加一个新的滑动动画效果，使菜单项从右侧滑入
                        val itemSlide by animateFloatAsState(
                            targetValue = if (isExpanded) 0f else 300f,
                            animationSpec = tween(
                                durationMillis = 500,
                                delayMillis = 100 * index,
                                easing = EaseOutQuart
                            ),
                            label = "itemSlideAnimation"
                        )
                        
                        // 为按钮添加上下弹跳动画效果
                        val itemBounce by animateFloatAsState(
                            targetValue = if (isExpanded) 0f else 20f,
                            animationSpec = tween(
                                durationMillis = 600,
                                delayMillis = 100 * index,
                                easing = EaseOutBounce
                            ),
                            label = "itemBounceAnimation"
                        )
                        
                        // 菜单项
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { 
                                    this.alpha = itemAlpha
                                    scaleX = itemScale
                                    scaleY = itemScale
                                    translationX = itemSlide
                                    translationY = itemBounce
                                }
                                .padding(vertical = 8.dp)
                                .clickable { menuItem.onClick() },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // 添加图标
                            Icon(
                                imageVector = menuItem.icon,
                                contentDescription = menuItem.title,
                                tint = if (isDarkMode) Color.White else Color.Black.copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // 标题和状态文字
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = menuItem.title,
                                    color = if (isDarkMode) Color.White else Color.Black.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                
                                Text(
                                    text = menuItem.statusText,
                                    color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                            
                            // 开关
                            Switch(
                                checked = menuItem.isEnabled,
                                onCheckedChange = { menuItem.onClick() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF2196F3),
                                    checkedTrackColor = Color(0xFF2196F3).copy(alpha = 0.5f),
                                    uncheckedThumbColor = if (isDarkMode) Color.Gray else Color.LightGray,
                                    uncheckedTrackColor = if (isDarkMode) Color.DarkGray else Color.LightGray.copy(alpha = 0.6f)
                                )
                            )
                        }
                        
                        // 不是最后一项添加分隔线
                        if (index < menuItems.size - 1) {
                            val dividerAlpha = itemAlpha
                            val dividerSlide by animateFloatAsState(
                                targetValue = if (isExpanded) 0f else 300f,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    delayMillis = 100 * (index + 1) + 50,
                                    easing = EaseOutQuart
                                ),
                                label = "dividerSlideAnimation"
                            )
                            
                            CustomMenuDivider(
                                color = if (isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.LightGray,
                                thickness = 0.5.dp,
                                modifier = Modifier
                                    .graphicsLayer { 
                                        this.alpha = dividerAlpha
                                        translationX = dividerSlide
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 添加数据类来存储菜单项信息
private data class MenuItemData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val statusText: String,
    val isEnabled: Boolean,
    val onClick: () -> Unit
) 