package com.rcbs.authenticator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 应用锁定屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockScreen(
    onUnlock: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String = "密码错误，请重试"
) {
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(isError) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(isError) {
        showError = isError
        if (isError) {
            password = ""
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 锁定图标
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "应用已锁定",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 标题
            Text(
                text = "应用已锁定",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 副标题
            Text(
                text = "请输入密码解锁",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 密码输入框
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    showError = false
                },
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (password.isNotEmpty()) {
                            keyboardController?.hide()
                            onUnlock(password)
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                isError = showError
            )
            
            // 错误提示
            if (showError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 解锁按钮
            Button(
                onClick = {
                    if (password.isNotEmpty()) {
                        keyboardController?.hide()
                        onUnlock(password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = password.isNotEmpty()
            ) {
                Text("解锁", fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 应用名称
            Text(
                text = "WearOTP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}