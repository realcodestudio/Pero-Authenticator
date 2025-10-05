package com.rcbs.authenticator.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rcbs.authenticator.data.ColorTheme
import com.rcbs.authenticator.data.ThemeMode
import com.rcbs.authenticator.ui.components.SetPasswordDialog
import com.rcbs.authenticator.ui.components.VerifyPasswordDialog
import com.rcbs.authenticator.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onThemeChanged: () -> Unit = {}, // 主题变化回调
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showVerifyPasswordDialog by remember { mutableStateOf(false) }
    var passwordVerificationError by remember { mutableStateOf(false) }
    
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
                    // 密码锁定
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "密码锁定",
                        subtitle = uiState.passwordDescription,
                        trailing = {
                            Switch(
                                checked = settings.passwordLockEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        if (uiState.hasPassword) {
                                            // 如果已有密码，直接启用
                                            viewModel.updatePasswordLockEnabled(true)
                                        } else {
                                            // 如果没有密码，显示设置密码对话框
                                            showSetPasswordDialog = true
                                        }
                                    } else {
                                        // 禁用密码锁定，需要验证密码
                                        if (uiState.hasPassword) {
                                            showVerifyPasswordDialog = true
                                        } else {
                                            viewModel.updatePasswordLockEnabled(false)
                                        }
                                    }
                                }
                            )
                        }
                    )
                    
                    // 修改密码（仅在已设置密码时显示）
                    if (uiState.hasPassword) {
                        SettingsItem(
                            icon = Icons.Default.Edit,
                            title = "修改密码",
                            subtitle = "更改应用锁定密码",
                            onClick = {
                                showVerifyPasswordDialog = true
                            }
                        )
                    }
                    
                    // 退到后台立刻锁定
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "退到后台立刻锁定",
                        subtitle = "应用进入后台时立即锁定",
                        trailing = {
                            Switch(
                                checked = settings.lockOnBackground,
                                onCheckedChange = viewModel::updateLockOnBackground
                            )
                        }
                    )
                    
                    // 自动锁定（仅在不是立刻锁定时显示）
                    if (!settings.lockOnBackground) {
                        SettingsItem(
                            icon = Icons.Default.Settings,
                            title = "自动锁定",
                            subtitle = "应用进入后台 ${settings.autoLockTimeout} 秒后锁定",
                            onClick = { showTimeoutDialog = true }
                        )
                    }
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
                        subtitle = "版本 ${viewModel.getAppInfo().version}",
                        onClick = { showAboutDialog = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "开源许可",
                        subtitle = "查看开源组件许可信息",
                        onClick = { showLicensesDialog = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Email,
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
    
    // 设置密码对话框
    if (showSetPasswordDialog) {
        SetPasswordDialog(
            onDismiss = { showSetPasswordDialog = false },
            onConfirm = { password ->
                if (viewModel.setPassword(password)) {
                    showSetPasswordDialog = false
                }
            },
            onValidateStrength = viewModel::validatePasswordStrength,
            getStrengthDescription = viewModel::getPasswordStrengthDescription
        )
    }
    
    // 验证密码对话框
    if (showVerifyPasswordDialog) {
        VerifyPasswordDialog(
            onDismiss = { 
                showVerifyPasswordDialog = false
                passwordVerificationError = false
            },
            onConfirm = { password ->
                if (viewModel.verifyPassword(password)) {
                    showVerifyPasswordDialog = false
                    passwordVerificationError = false
                    
                    if (settings.passwordLockEnabled) {
                        // 如果当前是启用状态，验证成功后禁用
                        viewModel.updatePasswordLockEnabled(false)
                    } else {
                        // 如果是要修改密码，显示设置新密码对话框
                        showSetPasswordDialog = true
                    }
                } else {
                    passwordVerificationError = true
                }
            },
            title = if (settings.passwordLockEnabled) "验证密码" else "修改密码",
            subtitle = if (settings.passwordLockEnabled) "请输入密码以禁用密码锁定" else "请输入当前密码",
            isError = passwordVerificationError
        )
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
                                        onThemeChanged() // 通知主题变化
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
                                        onThemeChanged() // 通知主题变化
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
    
    // 关于应用对话框
    if (showAboutDialog) {
        val appInfo = viewModel.getAppInfo()
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(appInfo.appName) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "版本 ${appInfo.version} (${appInfo.versionCode})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = appInfo.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "功能特性：",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "• 支持HOTP和TOTP算法\n• 手机与手表同步\n• 密码锁定保护\n• 数据备份与恢复\n• 多种主题颜色\n• QR码快速添加",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("确定")
                }
            }
        )
    }
    
    // 开源许可对话框
    if (showLicensesDialog) {
        Dialog(
            onDismissRequest = { showLicensesDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "开源许可",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(viewModel.getOpenSourceLicenses()) { license ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = license.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    Text(
                                        text = license.license,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = license.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showLicensesDialog = false }) {
                            Text("关闭")
                        }
                    }
                }
            }
        }
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