package com.rcbs.wearotp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rcbs.wearotp.data.OtpAccount
import com.rcbs.wearotp.data.OtpType
import com.rcbs.wearotp.repository.OtpRepository
import com.rcbs.wearotp.utils.OtpGenerator
import com.rcbs.wearotp.utils.OtpAuthData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OtpViewModel(private val repository: OtpRepository) : ViewModel() {
    
    private val _accounts = MutableStateFlow<List<OtpAccount>>(emptyList())
    val accounts: StateFlow<List<OtpAccount>> = _accounts.asStateFlow()
    
    private val _currentCodes = MutableStateFlow<Map<Long, String>>(emptyMap())
    val currentCodes: StateFlow<Map<Long, String>> = _currentCodes.asStateFlow()
    
    private val _remainingTime = MutableStateFlow(30L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()
    
    init {
        // 监听账户变化
        viewModelScope.launch {
            repository.getAllAccounts().collect { accountList ->
                _accounts.value = accountList
                updateOtpCodes()
            }
        }
        
        // 定时更新OTP代码
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
        val codes = mutableMapOf<Long, String>()
        _accounts.value.forEach { account ->
            try {
                val code = when (account.type) {
                    OtpType.TOTP -> OtpGenerator.generateTOTP(
                        secret = account.secret,
                        timeStepSeconds = account.period.toLong(),
                        digits = account.digits,
                        algorithm = "Hmac${account.algorithm}"
                    )
                    OtpType.HOTP -> OtpGenerator.generateHOTP(
                        secret = account.secret,
                        counter = 0, // 这里需要实现计数器逻辑
                        digits = account.digits,
                        algorithm = "Hmac${account.algorithm}"
                    )
                }
                codes[account.id] = code
            } catch (e: Exception) {
                codes[account.id] = "ERROR"
            }
        }
        _currentCodes.value = codes
    }
    
    private fun updateRemainingTime() {
        _remainingTime.value = OtpGenerator.getRemainingTime()
    }
    
    fun addAccount(account: OtpAccount) {
        viewModelScope.launch {
            repository.insertAccount(account)
        }
    }
    
    fun updateAccount(account: OtpAccount) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }
    
    fun deleteAccount(account: OtpAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }
    
    fun deleteAccountById(id: Long) {
        viewModelScope.launch {
            repository.deleteAccountById(id)
        }
    }
    
    fun addAccountFromUri(uri: String): Boolean {
        android.util.Log.d("OtpViewModel", "开始解析URI: $uri")
        
        return try {
            val otpData = OtpGenerator.parseOtpAuthUri(uri)
            if (otpData == null) {
                android.util.Log.e("OtpViewModel", "URI解析失败")
                return false
            }
            
            android.util.Log.d("OtpViewModel", "解析成功: type=${otpData.type}, account=${otpData.account}, issuer=${otpData.issuer}")
            
            val account = convertOtpAuthDataToAccount(otpData)
            android.util.Log.d("OtpViewModel", "创建账户对象: $account")
            
            addAccount(account)
            android.util.Log.d("OtpViewModel", "账户添加完成")
            true
        } catch (e: Exception) {
            android.util.Log.e("OtpViewModel", "添加账户时发生异常", e)
            false
        }
    }
    
    /**
     * 将 OtpAuthData 转换为 OtpAccount
     */
    private fun convertOtpAuthDataToAccount(otpData: OtpAuthData): OtpAccount {
        return OtpAccount(
            name = otpData.account.ifEmpty { "Unknown Account" },
            issuer = otpData.issuer.ifEmpty { "Unknown Issuer" },
            secret = otpData.secret,
            algorithm = otpData.algorithm,
            digits = otpData.digits,
            period = otpData.period,
            type = if (otpData.type.lowercase() == "totp") OtpType.TOTP else OtpType.HOTP
        )
    }
    
    /**
     * 测试GitHub setup key
     */
    fun testGitHubSetupKey(setupKey: String, username: String = "testuser"): Boolean {
        android.util.Log.d("OtpViewModel", "测试GitHub setup key: $setupKey")
        
        // 创建标准的GitHub URI格式
        val testUri = OtpGenerator.createGitHubTestUri(setupKey, username)
        android.util.Log.d("OtpViewModel", "生成的测试URI: $testUri")
        
        return addAccountFromUri(testUri)
    }
}