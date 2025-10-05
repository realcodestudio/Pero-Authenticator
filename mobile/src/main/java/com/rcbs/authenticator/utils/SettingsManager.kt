package com.rcbs.authenticator.utils

import android.content.Context
import android.content.SharedPreferences
import com.rcbs.authenticator.data.AppSettings
import com.rcbs.authenticator.data.ColorTheme
import com.rcbs.authenticator.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 设置管理器
 */
class SettingsManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "wearotp_settings", 
        Context.MODE_PRIVATE
    )
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    companion object {
        private const val KEY_PASSWORD_LOCK_ENABLED = "password_lock_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_COLOR_THEME = "color_theme"
        private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
        private const val KEY_LOCK_ON_BACKGROUND = "lock_on_background"
        private const val KEY_SHOW_ACCOUNT_ICONS = "show_account_icons"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    }
    
    /**
     * 从SharedPreferences加载设置
     */
    private fun loadSettings(): AppSettings {
        return AppSettings(
            passwordLockEnabled = prefs.getBoolean(KEY_PASSWORD_LOCK_ENABLED, false),
            themeMode = ThemeMode.valueOf(
                prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
            ),
            colorTheme = ColorTheme.valueOf(
                prefs.getString(KEY_COLOR_THEME, ColorTheme.DEFAULT.name) ?: ColorTheme.DEFAULT.name
            ),
            autoLockTimeout = prefs.getInt(KEY_AUTO_LOCK_TIMEOUT, 30),
            lockOnBackground = prefs.getBoolean(KEY_LOCK_ON_BACKGROUND, false),
            showAccountIcons = prefs.getBoolean(KEY_SHOW_ACCOUNT_ICONS, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        )
    }
    
    /**
     * 保存设置到SharedPreferences
     */
    private fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putBoolean(KEY_PASSWORD_LOCK_ENABLED, settings.passwordLockEnabled)
            putString(KEY_THEME_MODE, settings.themeMode.name)
            putString(KEY_COLOR_THEME, settings.colorTheme.name)
            putInt(KEY_AUTO_LOCK_TIMEOUT, settings.autoLockTimeout)
            putBoolean(KEY_LOCK_ON_BACKGROUND, settings.lockOnBackground)
            putBoolean(KEY_SHOW_ACCOUNT_ICONS, settings.showAccountIcons)
            putBoolean(KEY_VIBRATION_ENABLED, settings.vibrationEnabled)
            apply()
        }
    }
    
    /**
     * 更新密码锁定设置
     */
    fun updatePasswordLockEnabled(enabled: Boolean) {
        val newSettings = _settings.value.copy(passwordLockEnabled = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * 更新主题模式
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        val newSettings = _settings.value.copy(themeMode = themeMode)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * 更新颜色主题
     */
    fun updateColorTheme(colorTheme: ColorTheme) {
        val newSettings = _settings.value.copy(colorTheme = colorTheme)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * 更新自动锁定超时时间
     */
    fun updateAutoLockTimeout(timeout: Int) {
        val newSettings = _settings.value.copy(autoLockTimeout = timeout)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * 更新是否显示账户图标
     */
    fun updateShowAccountIcons(show: Boolean) {
        val newSettings = _settings.value.copy(showAccountIcons = show)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * 更新退到后台立刻锁定
     */
    fun updateLockOnBackground(enabled: Boolean) {
        val newSettings = _settings.value.copy(lockOnBackground = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
    
    /**
     * 更新是否启用震动
     */
    fun updateVibrationEnabled(enabled: Boolean) {
        val newSettings = _settings.value.copy(vibrationEnabled = enabled)
        _settings.value = newSettings
        saveSettings(newSettings)
    }
}