package com.example.time.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.compositionLocalOf

enum class CountdownTheme(val displayName: String) {
    Default("默认动态"),
    LiquidGlass("流体玻璃"),
    SoftNeumorphism("柔和拟物"),
    MinimalistZen("极简禅意"),
    Dark("黑暗模式")
}

data class ThemeProperties(
    val id: CountdownTheme,
    // Background can be a solid color or a brush (gradient)
    val backgroundBrush: Brush? = null,
    val backgroundColor: Color = Color.White,
    
    // Card properties
    val cardBackgroundColor: Color = Color.White,
    val cardContentColor: Color = Color.Black,
    val cardShape: Shape = RoundedCornerShape(8.dp),
    val cardElevation: Dp = 4.dp,
    val cardBorderWidth: Dp = 0.dp,
    val cardBorderColor: Color = Color.Transparent,
    val cardAlpha: Float = 1.0f,
    val hasTexture: Boolean = false,
    
    // Text properties
    val fontFamily: FontFamily = FontFamily.Default,
    val fontWeight: FontWeight = FontWeight.Bold,
    val textColor: Color = Color.Black,
    
    // Special flags
    val isDynamic: Boolean = false, // For Default theme which changes periodically
    val useBlurEffect: Boolean = false // For Liquid Glass
)

// Theme Definitions
object ThemeDefinitions {
    fun getTheme(theme: CountdownTheme, isDarkMode: Boolean): ThemeProperties {
        return when (theme) {
            CountdownTheme.Default -> ThemeProperties(
                id = CountdownTheme.Default,
                isDynamic = true,
                backgroundColor = Color.White,
                cardBackgroundColor = Color.White,
                cardContentColor = Color.Black,
                textColor = Color.Black
            )
            
            CountdownTheme.Dark -> ThemeProperties(
                id = CountdownTheme.Dark,
                backgroundColor = Color(0xFF212121),
                cardBackgroundColor = Color(0xFF333333),
                cardContentColor = Color.White,
                textColor = Color.White
            )
            
            CountdownTheme.LiquidGlass -> ThemeProperties(
                id = CountdownTheme.LiquidGlass,
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA), // Light Cyan
                        Color(0xFFB2EBF2),
                        Color(0xFF80DEEA)
                    )
                ),
                cardBackgroundColor = Color.White.copy(alpha = 0.6f),
                cardContentColor = Color(0xFF006064), // Dark Cyan
                cardShape = RoundedCornerShape(16.dp),
                cardElevation = 0.dp,
                cardBorderWidth = 1.dp,
                cardBorderColor = Color.White.copy(alpha = 0.5f),
                textColor = Color(0xFF006064),
                useBlurEffect = true
            )
            
            CountdownTheme.SoftNeumorphism -> ThemeProperties(
                id = CountdownTheme.SoftNeumorphism,
                backgroundColor = Color(0xFFE0E5EC),
                cardBackgroundColor = Color(0xFFE0E5EC),
                cardContentColor = Color(0xFF4A5568),
                cardShape = RoundedCornerShape(12.dp),
                cardElevation = 8.dp, // High elevation for neumorphic feel
                textColor = Color(0xFF4A5568)
            )
            
            CountdownTheme.MinimalistZen -> ThemeProperties(
                id = CountdownTheme.MinimalistZen,
                backgroundColor = Color(0xFFF9F9F7), // Rice paper color
                cardBackgroundColor = Color(0xFFEBEBE8),
                cardContentColor = Color(0xFF5D5D5D), // Dark Grey
                cardShape = RoundedCornerShape(2.dp),
                cardElevation = 0.dp,
                cardBorderWidth = 1.dp,
                cardBorderColor = Color(0xFFD3D3D3),
                textColor = Color(0xFF5D5D5D)
            )
        }
    }
}

val LocalThemeProperties = compositionLocalOf { ThemeDefinitions.getTheme(CountdownTheme.Default, false) }
