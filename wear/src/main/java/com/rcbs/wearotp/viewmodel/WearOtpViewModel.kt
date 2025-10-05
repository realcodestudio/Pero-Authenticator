package com.rcbs.wearotp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcbs.wearotp.data.WearOtpAccount
import com.rcbs.wearotp.data.WearOtpCode
import com.rcbs.wearotp.utils.OtpGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WearOtpViewModel : ViewModel() {
    
    // 模拟数据 - 在真实应用中，这些数据应该从手机同步过来
    private val _accounts = MutableStateFlow<List<WearOtpAccount>>(
        listOf(
            WearOtpAccount(
                id = 1,
                name = "john@example.com",
                issuer = "Google",
                secret = "JBSWY3DPEHPK3PXP", // 示例密钥
                algorithm = "SHA1",
                digits = 6,
                period = 30
            ),
            WearOtpAccount(
                id = 2,
                name = "user@github.com",
                issuer = "GitHub",
                secret = "JBSWY3DPEHPK3PXP", // 示例密钥
                algorithm = "SHA1",
                digits = 6,
                period = 30
            )
        )
    )
    val accounts: StateFlow<List<WearOtpAccount>> = _accounts.asStateFlow()
    
    private val _currentCodes = MutableStateFlow<List<WearOtpCode>>(emptyList())
    val currentCodes: StateFlow<List<WearOtpCode>> = _currentCodes.asStateFlow()
    
    private val _remainingTime = MutableStateFlow(30L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()
    
    init {
        startOtpTimer()
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