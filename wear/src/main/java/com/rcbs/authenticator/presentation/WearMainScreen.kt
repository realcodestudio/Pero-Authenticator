package com.rcbs.authenticator.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.rcbs.authenticator.presentation.components.WearOtpCard
import com.rcbs.authenticator.viewmodel.WearOtpViewModel

@Composable
fun WearMainScreen(
    viewModel: WearOtpViewModel = viewModel()
) {
    val accounts by viewModel.accounts.collectAsState()
    val currentCodes by viewModel.currentCodes.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    
    val listState = rememberScalingLazyListState()
    
    Scaffold(
        timeText = {
            TimeText(
                modifier = Modifier.scrollAway(listState)
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState
            )
        }
    ) {
        if (isLoading) {
            // 加载状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "正在同步...",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (accounts.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "无OTP账户",
                        style = MaterialTheme.typography.title3,
                        color = MaterialTheme.colors.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = syncStatus ?: "请在手机上导出数据",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 刷新按钮
                    Chip(
                        onClick = { viewModel.refreshSync() },
                        label = { Text("刷新同步") },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    )
                }
            }
        } else {
            // 账户列表
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    top = 32.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    ListHeader {
                        Text(
                            text = "Pero Authenticator",
                            style = MaterialTheme.typography.title3,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
                
                // 同步状态信息
                val statusText = syncStatus
                if (statusText != null && accounts.isNotEmpty()) {
                    item {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.caption1,
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                items(accounts) { account ->
                    val code = currentCodes.find { it.accountId == account.id }?.code ?: "------"
                    
                    WearOtpCard(
                        account = account,
                        otpCode = code,
                        remainingTime = remainingTime
                    )
                }
                
                // 刷新按钮
                if (accounts.isNotEmpty()) {
                    item {
                        Chip(
                            onClick = { viewModel.refreshSync() },
                            label = { Text("刷新同步") },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = MaterialTheme.colors.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

