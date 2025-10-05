package com.rcbs.authenticator.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rcbs.authenticator.data.AppSettings
import com.rcbs.authenticator.data.ColorTheme
import com.rcbs.authenticator.data.ThemeMode
import com.rcbs.authenticator.utils.PasswordManager
import com.rcbs.authenticator.utils.PasswordStrength
import com.rcbs.authenticator.utils.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsManager = SettingsManager(application)
    private val passwordManager = PasswordManager(application)
    
    val settings: StateFlow<AppSettings> = settingsManager.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        checkPasswordLockStatus()
    }
    
    private fun checkPasswordLockStatus() {
        val hasPassword = passwordManager.hasPassword()
        _uiState.value = _uiState.value.copy(
            hasPassword = hasPassword,
            passwordDescription = if (hasPassword) "已设置应用密码" else "未设置应用密码"
        )
    }
    
    fun setPassword(password: String): Boolean {
        return if (passwordManager.setPassword(password)) {
            viewModelScope.launch {
                settingsManager.updatePasswordLockEnabled(true)
                showMessage("应用密码设置成功")
                checkPasswordLockStatus()
            }
            true
        } else {
            showMessage("密码设置失败，请重试", true)
            false
        }
    }
    
    fun verifyPassword(password: String): Boolean {
        return passwordManager.verifyPassword(password)
    }
    
    fun clearPassword(): Boolean {
        return if (passwordManager.clearPassword()) {
            viewModelScope.launch {
                settingsManager.updatePasswordLockEnabled(false)
                showMessage("应用密码已清除")
                checkPasswordLockStatus()
            }
            true
        } else {
            showMessage("密码清除失败，请重试", true)
            false
        }
    }
    
    fun validatePasswordStrength(password: String): PasswordStrength {
        return passwordManager.validatePasswordStrength(password)
    }
    
    fun getPasswordStrengthDescription(strength: PasswordStrength): String {
        return passwordManager.getPasswordStrengthDescription(strength)
    }
    
    fun updatePasswordLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updatePasswordLockEnabled(enabled)
            showMessage(if (enabled) "密码锁定已启用" else "密码锁定已禁用")
        }
    }
    
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsManager.updateThemeMode(themeMode)
            showMessage("主题模式已更改为：${themeMode.displayName}")
        }
    }
    
    fun updateColorTheme(colorTheme: ColorTheme) {
        viewModelScope.launch {
            settingsManager.updateColorTheme(colorTheme)
            showMessage("颜色主题已更改为：${colorTheme.displayName}")
        }
    }
    
    fun updateAutoLockTimeout(timeout: Int) {
        viewModelScope.launch {
            settingsManager.updateAutoLockTimeout(timeout)
            val timeoutText = when (timeout) {
                15 -> "15秒"
                30 -> "30秒"
                60 -> "1分钟"
                300 -> "5分钟"
                600 -> "10分钟"
                else -> "${timeout}秒"
            }
            showMessage("自动锁定时间已设置为：$timeoutText")
        }
    }
    
    fun updateShowAccountIcons(show: Boolean) {
        viewModelScope.launch {
            settingsManager.updateShowAccountIcons(show)
            showMessage(if (show) "账户图标显示已启用" else "账户图标显示已禁用")
        }
    }
    
    fun updateLockOnBackground(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateLockOnBackground(enabled)
            showMessage(if (enabled) "退到后台立刻锁定已启用" else "退到后台立刻锁定已禁用")
        }
    }
    
    fun updateVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateVibrationEnabled(enabled)
            showMessage(if (enabled) "震动反馈已启用" else "震动反馈已禁用")
        }
    }
    
    fun showMessage(message: String, isError: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            message = message,
            isError = isError
        )
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            isError = false
        )
    }
    
    fun getPasswordManager(): PasswordManager = passwordManager
    
    fun getAppInfo(): AppInfo {
        val context = getApplication<Application>()
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            AppInfo(
                appName = "WearOTP",
                version = packageInfo.versionName ?: "1.0.0",
                versionCode = packageInfo.longVersionCode.toString(),
                description = "安全的双因素认证应用\n支持手机与手表同步\n保护您的在线账户安全"
            )
        } catch (e: Exception) {
            AppInfo(
                appName = "WearOTP",
                version = "1.0.0",
                versionCode = "1",
                description = "安全的双因素认证应用"
            )
        }
    }
    
    fun getOpenSourceLicenses(): List<OpenSourceLicense> {
        return listOf(
            OpenSourceLicense(
                name = "Android Jetpack Compose",
                license = "Apache License 2.0",
                description = "现代化的Android UI工具包"
            ),
            OpenSourceLicense(
                name = "Room Database",
                license = "Apache License 2.0", 
                description = "Android数据库持久化库"
            ),
            OpenSourceLicense(
                name = "Kotlin Coroutines",
                license = "Apache License 2.0",
                description = "Kotlin异步编程库"
            ),
            OpenSourceLicense(
                name = "Material Design Components",
                license = "Apache License 2.0",
                description = "Google Material Design组件库"
            ),
            OpenSourceLicense(
                name = "AndroidX Biometric",
                license = "Apache License 2.0",
                description = "Android生物认证支持库"
            ),
            OpenSourceLicense(
                name = "ML Kit Barcode Scanning",
                license = "Apache License 2.0",
                description = "Google机器学习条码扫描库"
            )
        )
    }
}

data class SettingsUiState(
    val hasPassword: Boolean = false,
    val passwordDescription: String = "",
    val message: String? = null,
    val isError: Boolean = false
)

data class AppInfo(
    val appName: String,
    val version: String,
    val versionCode: String,
    val description: String
)

data class OpenSourceLicense(
    val name: String,
    val license: String,
    val description: String
)