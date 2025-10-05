package com.rcbs.authenticator.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * 应用锁定状态管理器
 */
class AppLockManager(
    private val context: Context,
    private val settingsManager: SettingsManager,
    private val passwordManager: PasswordManager
) : DefaultLifecycleObserver {
    
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()
    
    private var backgroundTime: Long = 0
    private var lockTimer: ScheduledExecutorService? = null
    
    // 注意：需要在Activity中手动调用生命周期方法
    
    override fun onStart(owner: LifecycleOwner) {
        // 应用进入前台
        Log.d("AppLockManager", "onStart called")
        cancelLockTimer()
        checkIfShouldLock()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        // 应用进入后台
        Log.d("AppLockManager", "onStop called")
        val settings = settingsManager.settings.value
        
        Log.d("AppLockManager", "Password enabled: ${settings.passwordLockEnabled}, Has password: ${passwordManager.hasPassword()}")
        Log.d("AppLockManager", "Lock on background: ${settings.lockOnBackground}")
        
        if (!settings.passwordLockEnabled || !passwordManager.hasPassword()) {
            Log.d("AppLockManager", "Password not enabled or no password set, skipping lock")
            return
        }
        
        backgroundTime = System.currentTimeMillis()
        
        if (settings.lockOnBackground) {
            // 立即锁定
            Log.d("AppLockManager", "Locking immediately due to background setting")
            _isLocked.value = true
        } else {
            // 设置定时锁定
            Log.d("AppLockManager", "Setting lock timer for ${settings.autoLockTimeout} seconds")
            startLockTimer(settings.autoLockTimeout)
        }
    }
    
    /**
     * 检查是否应该锁定应用
     */
    private fun checkIfShouldLock() {
        val settings = settingsManager.settings.value
        
        Log.d("AppLockManager", "checkIfShouldLock called")
        
        if (!settings.passwordLockEnabled || !passwordManager.hasPassword()) {
            Log.d("AppLockManager", "Password not enabled, unlocking")
            _isLocked.value = false
            return
        }
        
        if (backgroundTime > 0) {
            val backgroundDuration = (System.currentTimeMillis() - backgroundTime) / 1000
            Log.d("AppLockManager", "Background duration: $backgroundDuration seconds")
            
            if (settings.lockOnBackground || backgroundDuration >= settings.autoLockTimeout) {
                Log.d("AppLockManager", "Should lock app")
                _isLocked.value = true
            }
        }
    }
    
    /**
     * 启动锁定定时器
     */
    private fun startLockTimer(timeoutSeconds: Int) {
        cancelLockTimer()
        
        lockTimer = Executors.newSingleThreadScheduledExecutor().apply {
            schedule({
                _isLocked.value = true
            }, timeoutSeconds.toLong(), TimeUnit.SECONDS)
        }
    }
    
    /**
     * 取消锁定定时器
     */
    private fun cancelLockTimer() {
        lockTimer?.shutdown()
        lockTimer = null
    }
    
    /**
     * 解锁应用
     */
    fun unlock() {
        _isLocked.value = false
        backgroundTime = 0
        cancelLockTimer()
    }
    
    /**
     * 手动锁定应用
     */
    fun lock() {
        _isLocked.value = true
    }
    
    /**
     * 检查是否启用了密码锁定
     */
    fun isPasswordLockEnabled(): Boolean {
        val settings = settingsManager.settings.value
        return settings.passwordLockEnabled && passwordManager.hasPassword()
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        cancelLockTimer()
    }
}