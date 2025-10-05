package com.rcbs.authenticator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rcbs.authenticator.data.OtpAccount
import com.rcbs.authenticator.data.OtpDatabase
import com.rcbs.authenticator.repository.OtpRepository
import com.rcbs.authenticator.sync.WearSyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    
    // 需要通过依赖注入获取这些实例，这里暂时注释掉
    private val repository = OtpRepository(OtpDatabase.getDatabase(application).otpDao())
    private val syncManager = WearSyncManager(application)
    
    val accounts: StateFlow<List<OtpAccount>> = repository.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()
    
    /**
     * 导出数据到手表
     */
    fun exportToWear() {
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "正在导出数据..."
            
            try {
                val currentAccounts = accounts.value
                if (currentAccounts.isEmpty()) {
                    _syncStatus.value = "没有可同步的账户"
                    return@launch
                }
                
                val result = syncManager.exportToWear(currentAccounts)
                result.fold(
                    onSuccess = { message ->
                        _syncStatus.value = "导出成功！共同步${currentAccounts.size}个账户"
                    },
                    onFailure = { error ->
                        _syncStatus.value = "导出失败: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _syncStatus.value = "导出失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}