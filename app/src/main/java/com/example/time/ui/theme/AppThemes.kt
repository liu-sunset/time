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
    SoftNeumorphism("柔和拟物"),
    VintagePaper("复古纸张"),
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
    val cardCornerRadius: Dp = 8.dp,
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
    val isDynamic: Boolean = false // For Default theme which changes periodically
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
            
            CountdownTheme.SoftNeumorphism -> ThemeProperties(
                id = CountdownTheme.SoftNeumorphism,
                backgroundColor = Color(0xFFE6E1D6), // Warm Paper/Linen
                cardBackgroundColor = Color(0xFFE6E1D6),
                cardContentColor = Color(0xFF5D5550), // Warm Dark Grey
                cardCornerRadius = 16.dp,
                cardElevation = 6.dp, // Softer elevation
                textColor = Color(0xFF5D5550),
                hasTexture = true // Add texture for "Material/Paper" feel
            )
            
            CountdownTheme.VintagePaper -> ThemeProperties(
                id = CountdownTheme.VintagePaper,
                backgroundColor = Color(0xFFF5E6D3), // Textured beige/light brown
                cardBackgroundColor = Color(0xFFE8DCCA), // Slightly darker
                cardContentColor = Color(0xFF5C4033), // Dark Brown (Sepia)
                cardCornerRadius = 2.dp, // Irregular/rough look (simulated by small radius)
                cardElevation = 2.dp,
                textColor = Color(0xFF5C4033),
                fontFamily = FontFamily.Monospace, // Classic Typewriter
                hasTexture = true // Fiber/aged spots effect
            )
        }
    }
}

val LocalThemeProperties = compositionLocalOf { ThemeDefinitions.getTheme(CountdownTheme.Default, false) }
