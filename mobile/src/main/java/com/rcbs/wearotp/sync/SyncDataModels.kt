package com.rcbs.wearotp.sync

import kotlinx.serialization.Serializable

/**
 * 用于同步和备份的数据模型
 */
@Serializable
data class SyncOtpAccount(
    val id: Long = 0,
    val name: String,
    val issuer: String,
    val secret: String,
    val type: String, // "TOTP" or "HOTP"
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val counter: Long = 0
)

/**
 * 备份文件数据结构
 */
@Serializable
data class OtpBackupData(
    val version: String = "1.0",
    val timestamp: Long = System.currentTimeMillis(),
    val accounts: List<SyncOtpAccount>
)

/**
 * Wear OS 数据同步消息
 */
@Serializable
data class SyncMessage(
    val type: SyncMessageType,
    val data: String // JSON 序列化的数据
)

@Serializable
enum class SyncMessageType {
    FULL_SYNC,      // 完整同步
    ADD_ACCOUNT,    // 添加账户
    UPDATE_ACCOUNT, // 更新账户
    DELETE_ACCOUNT, // 删除账户
    BACKUP_REQUEST, // 备份请求
    BACKUP_DATA     // 备份数据
}

/**
 * 同步状态
 */
data class SyncStatus(
    val isConnected: Boolean = false,
    val lastSyncTime: Long = 0,
    val syncInProgress: Boolean = false,
    val error: String? = null
)