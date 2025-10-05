package com.rcbs.authenticator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rcbs.authenticator.data.WearOtpAccount
import com.rcbs.authenticator.data.WearOtpCode
import com.rcbs.authenticator.sync.WearSyncReader
import com.rcbs.authenticator.utils.OtpGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WearOtpViewModel(application: Application) : AndroidViewModel(application) {
    
    private val syncReader = WearSyncReader(application)
    
    // 从手机同步的账户数据
    private val _accounts = MutableStateFlow<List<WearOtpAccount>>(emptyList())
    val accounts: StateFlow<List<WearOtpAccount>> = _accounts.asStateFlow()
    
    private val _currentCodes = MutableStateFlow<List<WearOtpCode>>(emptyList())
    val currentCodes: StateFlow<List<WearOtpCode>> = _currentCodes.asStateFlow()
    
    private val _remainingTime = MutableStateFlow(30L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()
    
    init {
        loadAccountsFromSync()
        startOtpTimer()
    }
    
    /**
     * 从手机同步数据
     */
    private fun loadAccountsFromSync() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "正在同步..."
            
            try {
                val result = syncReader.readFromPhone()
                result.fold(
                    onSuccess = { accounts ->
                        _accounts.value = accounts
                        _syncStatus.value = if (accounts.isEmpty()) {
                            "未找到同步数据，请在手机上导出数据"
                        } else {
                            "同步成功，共${accounts.size}个账户"
                        }
                    },
                    onFailure = { error ->
                        _syncStatus.value = "同步失败: ${error.message}"
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 手动刷新同步数据
     */
    fun refreshSync() {
        loadAccountsFromSync()
    }
    
    private fun startOtpTimer() {
        viewModelScope.launch {
            while (true) {
                updateOtpCodes()
                updateRemainingTime()
                kotlinx.coroutines.delay(1000) // 每秒更新
            }
        }
    }
    
    private fun updateOtpCodes() {
        val codes = _accounts.value.map { account ->
            try {
                val code = OtpGenerator.generateTOTP(
                    secret = account.secret,
                    timeStepSeconds = account.period.toLong(),
                    digits = account.digits,
                    algorithm = "Hmac${account.algorithm}"
                )
                WearOtpCode(
                    accountId = account.id,
                    code = code,
                    remainingTime = _remainingTime.value
                )
            } catch (e: Exception) {
                WearOtpCode(
                    accountId = account.id,
                    code = "ERROR",
                    remainingTime = _remainingTime.value
                )
            }
        }
        _currentCodes.value = codes
    }
    
    private fun updateRemainingTime() {
        _remainingTime.value = OtpGenerator.getRemainingTime()
    }
    
    fun getCodeForAccount(accountId: Long): String? {
        return _currentCodes.value.find { it.accountId == accountId }?.code
    }
}