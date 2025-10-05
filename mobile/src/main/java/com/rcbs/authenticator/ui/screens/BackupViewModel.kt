package com.rcbs.authenticator.ui.screens

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rcbs.authenticator.backup.BackupInfo
import com.rcbs.authenticator.backup.SimpleBackupManager
import com.rcbs.authenticator.data.OtpDatabase
import com.rcbs.authenticator.repository.OtpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val showImportDialog: Boolean = false,
    val backupInfo: BackupInfo? = null,
    val selectedUri: Uri? = null
)

class BackupViewModel(application: Application) : AndroidViewModel(application) {
    private val database = OtpDatabase.getDatabase(application)
    private val repository = OtpRepository(database.otpDao())
    private val backupManager = SimpleBackupManager(application, repository)
    
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()
    
    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            
            backupManager.exportBackup(uri)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = message,
                        isError = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "导出失败: ${error.message}",
                        isError = true
                    )
                }
        }
    }
    
    fun validateBackupFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            
            backupManager.validateBackupFile(uri)
                .onSuccess { backupInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showImportDialog = true,
                        backupInfo = backupInfo,
                        selectedUri = uri
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "备份文件验证失败: ${error.message}",
                        isError = true
                    )
                }
        }
    }
    
    fun importBackup(replaceExisting: Boolean) {
        val uri = _uiState.value.selectedUri ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                showImportDialog = false,
                message = null
            )
            
            backupManager.importBackup(uri, replaceExisting)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = message,
                        isError = false,
                        backupInfo = null,
                        selectedUri = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "导入失败: ${error.message}",
                        isError = true,
                        backupInfo = null,
                        selectedUri = null
                    )
                }
        }
    }
    
    fun createQuickBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            
            backupManager.createQuickBackup()
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = message,
                        isError = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "快速备份失败: ${error.message}",
                        isError = true
                    )
                }
        }
    }
    
    fun dismissImportDialog() {
        _uiState.value = _uiState.value.copy(
            showImportDialog = false,
            backupInfo = null,
            selectedUri = null
        )
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    fun generateBackupFileName(): String {
        return backupManager.generateBackupFileName()
    }
}