package com.rcbs.wearotp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rcbs.wearotp.data.ColorTheme
import com.rcbs.wearotp.data.ThemeMode
import com.rcbs.wearotp.utils.BiometricAvailability
import com.rcbs.wearotp.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    
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
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 安全设置
            item {
                SettingsSection(title = "安全") {
                    // 生物认证
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "生物认证",
                        subtitle = uiState.biometricDescription,
                        trailing = {
                            Switch(
                                checked = settings.biometricEnabled,
                                enabled = uiState.biometricAvailability == BiometricAvailability.AVAILABLE,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        // 显示生物认证提示来验证
                                        val activity = context as? FragmentActivity
                                        if (activity != null) {
                                            viewModel.getBiometricManager().showBiometricPrompt(
                                                activity = activity,
                                                title = "启用生物认证",
                                                subtitle = "请验证您的身份以启用生物认证保护",
                                                onSuccess = {
                                                    viewModel.updateBiometricEnabled(true)
                                                    viewModel.showMessage("生物认证已启用")
                                                },
                                                onError = { error ->
                                                    viewModel.showMessage("认证失败: $error", true)
                                                },
                                                onCancel = {
                                                    viewModel.showMessage("已取消启用生物认证")
                                                }
                                            )
                                        }
                                    } else {
                                        viewModel.updateBiometricEnabled(false)
                                        viewModel.showMessage("生物认证已禁用")
                                    }
                                }
                            )
                        }
                    )
                    
                    // 自动锁定
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "自动锁定",
                        subtitle = "应用进入后台 ${settings.autoLockTimeout} 秒后锁定",
                        onClick = { showTimeoutDialog = true }
                    )
                }
            }
            
            // 外观设置
            item {
                SettingsSection(title = "外观") {
                    // 主题模式
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "主题模式",
                        subtitle = settings.themeMode.displayName,
                        onClick = { showThemeDialog = true }
                    )
                    
                    // 颜色主题
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "颜色主题",
                        subtitle = settings.colorTheme.displayName,
                        onClick = { showColorDialog = true }
                    )
                    
                    // 显示账户图标
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "显示账户图标",
                        subtitle = "在账户列表中显示服务商图标",
                        trailing = {
                            Switch(
                                checked = settings.showAccountIcons,
                                onCheckedChange = viewModel::updateShowAccountIcons
                            )
                        }
                    )
                }
            }
            
            // 行为设置
            item {
                SettingsSection(title = "行为") {
                    // 震动反馈
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "震动反馈",
                        subtitle = "操作时提供触觉反馈",
                        trailing = {
                            Switch(
                                checked = settings.vibrationEnabled,
                                onCheckedChange = viewModel::updateVibrationEnabled
                            )
                        }
                    )
                }
            }
            
            // 数据管理
            item {
                SettingsSection(title = "数据管理") {
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "备份与恢复",
                        subtitle = "导出或导入OTP账户数据",
                        onClick = onNavigateToBackup
                    )
                }
            }
            
            // 关于
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "关于 WearOTP",
                        subtitle = "版本 1.0.0",
                        onClick = {
                            viewModel.showMessage("WearOTP - 安全的双因素认证应用\n支持手机与手表同步")
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "开源许可",
                        subtitle = "查看开源组件许可信息",
                        onClick = {
                            // 可以打开一个许可页面或对话框
                            viewModel.showMessage("本应用使用了多个开源组件")
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "反馈问题",
                        subtitle = "报告bug或提出建议",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@wearotp.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "WearOTP反馈")
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                viewModel.showMessage("未找到邮件应用", true)
                            }
                        }
                    )
                }
            }
            
            // 消息显示
            uiState.message?.let { message ->
                item {
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
    
    // 主题模式选择对话框
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("选择主题模式") },
            text = {
                Column(Modifier.selectableGroup()) {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = settings.themeMode == mode,
                                    onClick = {
                                        viewModel.updateThemeMode(mode)
                                        showThemeDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.themeMode == mode,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(mode.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
    
    // 颜色主题选择对话框
    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("选择颜色主题") },
            text = {
                Column(Modifier.selectableGroup()) {
                    ColorTheme.values().forEach { theme ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = settings.colorTheme == theme,
                                    onClick = {
                                        viewModel.updateColorTheme(theme)
                                        showColorDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.colorTheme == theme,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
    
    // 自动锁定时间选择对话框
    if (showTimeoutDialog) {
        val timeoutOptions = listOf(
            15 to "15秒",
            30 to "30秒",
            60 to "1分钟",
            300 to "5分钟",
            600 to "10分钟"
        )
        
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text("自动锁定时间") },
            text = {
                Column(Modifier.selectableGroup()) {
                    timeoutOptions.forEach { (timeout, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = settings.autoLockTimeout == timeout,
                                    onClick = {
                                        viewModel.updateAutoLockTimeout(timeout)
                                        showTimeoutDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.autoLockTimeout == timeout,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                if (onClick != null) {
                    modifier.clickable { onClick() }
                } else {
                    modifier
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (trailing != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }
}