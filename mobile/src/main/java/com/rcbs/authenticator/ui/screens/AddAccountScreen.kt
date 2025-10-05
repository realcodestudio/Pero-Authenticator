package com.rcbs.authenticator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rcbs.authenticator.data.OtpAccount
import com.rcbs.authenticator.data.OtpType
import com.rcbs.authenticator.utils.OtpGenerator
import com.rcbs.authenticator.viewmodel.OtpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onNavigateBack: () -> Unit,
    viewModel: OtpViewModel = viewModel()
) {
    var accountName by remember { mutableStateOf("") }
    var issuer by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var algorithm by remember { mutableStateOf("SHA1") }
    var digits by remember { mutableStateOf("6") }
    var period by remember { mutableStateOf("30") }
    var otpType by remember { mutableStateOf(OtpType.TOTP) }
    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val algorithms = listOf("SHA1", "SHA256", "SHA512")
    val digitOptions = listOf("6", "8")
    val periodOptions = listOf("15", "30", "60")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加账户") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (validateInput(accountName, secret)) {
                                val account = OtpAccount(
                                    name = accountName.trim(),
                                    issuer = issuer.trim(),
                                    secret = secret.trim().uppercase(),
                                    algorithm = algorithm,
                                    digits = digits.toInt(),
                                    period = period.toInt(),
                                    type = otpType
                                )
                                viewModel.addAccount(account)
                                onNavigateBack()
                            } else {
                                showError = true
                                errorMessage = "请填写账户名和有效的密钥"
                            }
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 错误提示
            if (showError) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // 账户名
            OutlinedTextField(
                value = accountName,
                onValueChange = { 
                    accountName = it
                    showError = false
                },
                label = { Text("账户名 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 发行商
            OutlinedTextField(
                value = issuer,
                onValueChange = { issuer = it },
                label = { Text("发行商") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 密钥
            OutlinedTextField(
                value = secret,
                onValueChange = { 
                    secret = it
                    showError = false
                },
                label = { Text("密钥 *") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Base32编码的密钥") }
            )
            
            // OTP类型
            var expandedType by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    value = otpType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    OtpType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                otpType = type
                                expandedType = false
                            }
                        )
                    }
                }
            }
            
            // 算法
            var expandedAlgorithm by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedAlgorithm,
                onExpandedChange = { expandedAlgorithm = !expandedAlgorithm }
            ) {
                OutlinedTextField(
                    value = algorithm,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("算法") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAlgorithm) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedAlgorithm,
                    onDismissRequest = { expandedAlgorithm = false }
                ) {
                    algorithms.forEach { alg ->
                        DropdownMenuItem(
                            text = { Text(alg) },
                            onClick = {
                                algorithm = alg
                                expandedAlgorithm = false
                            }
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 位数
                var expandedDigits by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedDigits,
                    onExpandedChange = { expandedDigits = !expandedDigits },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = digits,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("位数") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDigits) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDigits,
                        onDismissRequest = { expandedDigits = false }
                    ) {
                        digitOptions.forEach { digit ->
                            DropdownMenuItem(
                                text = { Text(digit) },
                                onClick = {
                                    digits = digit
                                    expandedDigits = false
                                }
                            )
                        }
                    }
                }
                
                // 周期（仅TOTP）
                if (otpType == OtpType.TOTP) {
                    var expandedPeriod by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedPeriod,
                        onExpandedChange = { expandedPeriod = !expandedPeriod },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "$period 秒",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("周期") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPeriod) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPeriod,
                            onDismissRequest = { expandedPeriod = false }
                        ) {
                            periodOptions.forEach { per ->
                                DropdownMenuItem(
                                    text = { Text("$per 秒") },
                                    onClick = {
                                        period = per
                                        expandedPeriod = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun validateInput(accountName: String, secret: String): Boolean {
    return accountName.trim().isNotEmpty() && 
           secret.trim().isNotEmpty() && 
           OtpGenerator.isValidSecret(secret.trim())
}