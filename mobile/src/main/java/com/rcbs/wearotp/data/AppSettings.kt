package com.rcbs.wearotp.data

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 应用设置数据类
 */
data class AppSettings(
    val biometricEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorTheme: ColorTheme = ColorTheme.DEFAULT,
    val autoLockTimeout: Int = 30, // 秒
    val showAccountIcons: Boolean = true,
    val vibrationEnabled: Boolean = true
)

/**
 * 主题模式
 */
enum class ThemeMode(val displayName: String) {
    LIGHT("浅色模式"),
    DARK("深色模式"),
    SYSTEM("跟随系统")
}

/**
 * 颜色主题
 */
enum class ColorTheme(val displayName: String) {
    DEFAULT("默认蓝色"),
    GREEN("绿色"),
    PURPLE("紫色"),
    ORANGE("橙色"),
    RED("红色"),
    PINK("粉色")
}

/**
 * 获取颜色主题的配色方案
 */
fun ColorTheme.getLightColorScheme(): ColorScheme {
    return when (this) {
        ColorTheme.DEFAULT -> lightColorScheme()
        ColorTheme.GREEN -> lightColorScheme(
            primary = Color(0xFF2E7D32),
            primaryContainer = Color(0xFFC8E6C9),
            secondary = Color(0xFF4CAF50),
            secondaryContainer = Color(0xFFE8F5E8)
        )
        ColorTheme.PURPLE -> lightColorScheme(
            primary = Color(0xFF7B1FA2),
            primaryContainer = Color(0xFFE1BEE7),
            secondary = Color(0xFF9C27B0),
            secondaryContainer = Color(0xFFF3E5F5)
        )
        ColorTheme.ORANGE -> lightColorScheme(
            primary = Color(0xFFE65100),
            primaryContainer = Color(0xFFFFE0B2),
            secondary = Color(0xFFFF9800),
            secondaryContainer = Color(0xFFFFF3E0)
        )
        ColorTheme.RED -> lightColorScheme(
            primary = Color(0xFFD32F2F),
            primaryContainer = Color(0xFFFFCDD2),
            secondary = Color(0xFFF44336),
            secondaryContainer = Color(0xFFFFEBEE)
        )
        ColorTheme.PINK -> lightColorScheme(
            primary = Color(0xFFC2185B),
            primaryContainer = Color(0xFFF8BBD9),
            secondary = Color(0xFFE91E63),
            secondaryContainer = Color(0xFFFCE4EC)
        )
    }
}

fun ColorTheme.getDarkColorScheme(): ColorScheme {
    return when (this) {
        ColorTheme.DEFAULT -> darkColorScheme()
        ColorTheme.GREEN -> darkColorScheme(
            primary = Color(0xFF81C784),
            primaryContainer = Color(0xFF2E7D32),
            secondary = Color(0xFFA5D6A7),
            secondaryContainer = Color(0xFF388E3C)
        )
        ColorTheme.PURPLE -> darkColorScheme(
            primary = Color(0xFFBA68C8),
            primaryContainer = Color(0xFF7B1FA2),
            secondary = Color(0xFFCE93D8),
            secondaryContainer = Color(0xFF8E24AA)
        )
        ColorTheme.ORANGE -> darkColorScheme(
            primary = Color(0xFFFFB74D),
            primaryContainer = Color(0xFFE65100),
            secondary = Color(0xFFFFCC02),
            secondaryContainer = Color(0xFFF57C00)
        )
        ColorTheme.RED -> darkColorScheme(
            primary = Color(0xFFEF5350),
            primaryContainer = Color(0xFFD32F2F),
            secondary = Color(0xFFE57373),
            secondaryContainer = Color(0xFFF44336)
        )
        ColorTheme.PINK -> darkColorScheme(
            primary = Color(0xFFF06292),
            primaryContainer = Color(0xFFC2185B),
            secondary = Color(0xFFF48FB1),
            secondaryContainer = Color(0xFFE91E63)
        )
    }
}