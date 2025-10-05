package com.rcbs.wearotp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rcbs.wearotp.data.AppSettings
import com.rcbs.wearotp.data.ColorTheme
import com.rcbs.wearotp.data.ThemeMode
import com.rcbs.wearotp.utils.BiometricAuthManager
import com.rcbs.wearotp.utils.BiometricAvailability
import com.rcbs.wearotp.utils.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsManager = SettingsManager(application)
    private val biometricManager = BiometricAuthManager(application)
    
    val settings: StateFlow<AppSettings> = settingsManager.settings
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        checkBiometricAvailability()
    }
    
    private fun checkBiometricAvailability() {
        val availability = biometricManager.isBiometricAvailable()
        val description = biometricManager.getBiometricAvailabilityDescription()
        
        _uiState.value = _uiState.value.copy(
            biometricAvailability = availability,
            biometricDescription = description
        )
    }
    
    fun updateBiometricEnabled(enabled: Boolean) {
        settingsManager.updateBiometricEnabled(enabled)
    }
    
    fun updateThemeMode(themeMode: ThemeMode) {
        settingsManager.updateThemeMode(themeMode)
    }
    
    fun updateColorTheme(colorTheme: ColorTheme) {
        settingsManager.updateColorTheme(colorTheme)
    }
    
    fun updateAutoLockTimeout(timeout: Int) {
        settingsManager.updateAutoLockTimeout(timeout)
    }
    
    fun updateShowAccountIcons(show: Boolean) {
        settingsManager.updateShowAccountIcons(show)
    }
    
    fun updateVibrationEnabled(enabled: Boolean) {
        settingsManager.updateVibrationEnabled(enabled)
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
    
    fun getBiometricManager(): BiometricAuthManager = biometricManager
}

data class SettingsUiState(
    val biometricAvailability: BiometricAvailability = BiometricAvailability.UNKNOWN,
    val biometricDescription: String = "",
    val message: String? = null,
    val isError: Boolean = false
)