package com.rcbs.authenticator.ui.components

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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rcbs.authenticator.utils.PasswordStrength

/**
 * 设置密码对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onValidateStrength: (String) -> PasswordStrength,
    getStrengthDescription: (PasswordStrength) -> String
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val passwordStrength = onValidateStrength(password)
    val passwordsMatch = password == confirmPassword && password.isNotEmpty()
    val canConfirm = passwordsMatch && passwordStrength != PasswordStrength.EMPTY && passwordStrength != PasswordStrength.TOO_SHORT
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "设置应用密码",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "设置一个密码来保护您的OTP账户",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 密码输入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("输入密码") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Lock else Icons.Default.Lock,
                                contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true
                )
                
                // 密码强度指示器
                if (password.isNotEmpty()) {
                    Column {
                        Text(
                            text = getStrengthDescription(passwordStrength),
                            style = MaterialTheme.typography.bodySmall,
                            color = when (passwordStrength) {
                                PasswordStrength.EMPTY, PasswordStrength.TOO_SHORT -> MaterialTheme.colorScheme.error
                                PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                                PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.primary
                                PasswordStrength.STRONG -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LinearProgressIndicator(
                            progress = when (passwordStrength) {
                                PasswordStrength.EMPTY, PasswordStrength.TOO_SHORT -> 0.0f
                                PasswordStrength.WEAK -> 0.33f
                                PasswordStrength.MEDIUM -> 0.66f
                                PasswordStrength.STRONG -> 1.0f
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = when (passwordStrength) {
                                PasswordStrength.EMPTY, PasswordStrength.TOO_SHORT -> MaterialTheme.colorScheme.error
                                PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                                PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.primary
                                PasswordStrength.STRONG -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                    }
                }
                
                // 确认密码输入框
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("确认密码") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Lock else Icons.Default.Lock,
                                contentDescription = if (confirmPasswordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canConfirm) {
                                keyboardController?.hide()
                                onConfirm(password)
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && !passwordsMatch
                )
                
                // 密码匹配提示
                if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    Text(
                        text = "两次输入的密码不一致",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    keyboardController?.hide()
                    onConfirm(password)
                },
                enabled = canConfirm
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 验证密码对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String = "验证密码",
    subtitle: String = "请输入应用密码以继续",
    isError: Boolean = false,
    errorMessage: String = "密码错误，请重试"
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Lock else Icons.Default.Lock,
                                contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (password.isNotEmpty()) {
                                keyboardController?.hide()
                                onConfirm(password)
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    isError = isError
                )
                
                if (isError) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    keyboardController?.hide()
                    onConfirm(password)
                },
                enabled = password.isNotEmpty()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}