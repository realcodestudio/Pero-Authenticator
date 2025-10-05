package com.rcbs.authenticator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rcbs.authenticator.data.OtpDatabase
import com.rcbs.authenticator.navigation.MainNavigation
import com.rcbs.authenticator.repository.OtpRepository
import com.rcbs.authenticator.ui.screens.AppLockScreen
import com.rcbs.authenticator.ui.theme.PeroAuthenticatorTheme
import com.rcbs.authenticator.utils.AppLockManager
import com.rcbs.authenticator.utils.PasswordManager
import com.rcbs.authenticator.utils.SettingsManager
import com.rcbs.authenticator.viewmodel.OtpViewModel
import com.rcbs.authenticator.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    
    private lateinit var appLockManager: AppLockManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化数据库和Repository
        val database = OtpDatabase.getDatabase(this)
        val repository = OtpRepository(database.otpDao())
        
        // 初始化管理器
        val settingsManager = SettingsManager(this)
        val passwordManager = PasswordManager(this)
        appLockManager = AppLockManager(this, settingsManager, passwordManager)
        
        // 将AppLockManager添加到生命周期观察者
        lifecycle.addObserver(appLockManager)
        
        setContent {
            val settings by settingsManager.settings.collectAsState()
            val isLocked by appLockManager.isLocked.collectAsState()
            var themeKey by remember { mutableStateOf(0) }
            var passwordError by remember { mutableStateOf(false) }
            
            // 使用key来强制重组，实现主题实时切换
            key(themeKey) {
                PeroAuthenticatorTheme(
                    themeMode = settings.themeMode,
                    colorTheme = settings.colorTheme
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (isLocked && appLockManager.isPasswordLockEnabled()) {
                            AppLockScreen(
                                onUnlock = { password ->
                                    if (passwordManager.verifyPassword(password)) {
                                        appLockManager.unlock()
                                        passwordError = false
                                    } else {
                                        passwordError = true
                                    }
                                },
                                isError = passwordError
                            )
                        } else {
                            MainNavigation()
                        }
                    }
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        if (::appLockManager.isInitialized) {
            appLockManager.onStart(this)
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (::appLockManager.isInitialized) {
            appLockManager.onStop(this)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::appLockManager.isInitialized) {
            lifecycle.removeObserver(appLockManager)
            appLockManager.cleanup()
        }
    }
}