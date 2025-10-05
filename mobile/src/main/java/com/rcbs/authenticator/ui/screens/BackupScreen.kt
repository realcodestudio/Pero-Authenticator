package com.rcbs.authenticator.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // 文件选择器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.exportBackup(uri)
            }
        }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.validateBackupFile(uri)
            }
        }
    }
    
    // 确认对话框
    if (uiState.showImportDialog && uiState.backupInfo != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportDialog() },
            title = { Text("确认导入备份") },
            text = {
                Column {
                    Text("备份文件信息:")
                    Text("• 版本: ${uiState.backupInfo?.version}")
                    Text("• 账户数量: ${uiState.backupInfo?.accountCount}")
                    val date = Date(uiState.backupInfo?.timestamp ?: 0L)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    Text("• 创建时间: ${dateFormat.format(date)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("选择导入方式:")
                }
            },
            confirmButton = {
                Column {
                    TextButton(
                        onClick = { viewModel.importBackup(replaceExisting = false) }
                    ) {
                        Text("合并导入")
                    }
                    TextButton(
                        onClick = { viewModel.importBackup(replaceExisting = true) }
                    ) {
                        Text("替换导入", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissImportDialog() }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 消息提示
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("备份与恢复") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "导出备份",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("将所有 OTP 账户导出到备份文件")
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_TITLE, viewModel.generateBackupFileName())
                                }
                                exportLauncher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Text("导出到文件")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.createQuickBackup() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Text("快速备份到本地")
                        }
                    }
                }
            }
            
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "导入备份",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("从备份文件恢复 OTP 账户")
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                                        "application/json",
                                        "application/octet-stream",
                                        "text/plain"
                                    ))
                                }
                                importLauncher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Text("选择备份文件")
                        }
                    }
                }
            }
            
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "备份说明",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("• 备份文件包含所有 OTP 账户的完整信息")
                            Text("• 合并导入：保留现有账户，添加新账户")
                            Text("• 替换导入：删除现有账户，仅保留备份中的账户")
                            Text("• 快速备份：保存到应用内部存储")
                            Text("• 请妥善保管备份文件，避免泄露")
                        }
                    }
                }
            }
            
            // 状态显示
            item {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.message?.let { message ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isError) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = if (uiState.isError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }
        }
    }
}