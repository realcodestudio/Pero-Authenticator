package com.rcbs.wearotp.sync

import com.rcbs.wearotp.data.OtpAccount
import com.rcbs.wearotp.data.OtpType

/**
 * 数据转换工具类
 */
object DataConverter {
    
    /**
     * 将数据库实体转换为同步模型
     */
    fun toSyncModel(account: OtpAccount): SyncOtpAccount {
        return SyncOtpAccount(
            id = account.id,
            name = account.name,
            issuer = account.issuer,
            secret = account.secret,
            type = account.type.name,
            algorithm = account.algorithm,
            digits = account.digits,
            period = account.period,
            counter = account.counter
        )
    }
    
    /**
     * 将同步模型转换为数据库实体
     */
    fun fromSyncModel(syncAccount: SyncOtpAccount): OtpAccount {
        return OtpAccount(
            id = syncAccount.id,
            name = syncAccount.name,
            issuer = syncAccount.issuer,
            secret = syncAccount.secret,
            type = when (syncAccount.type) {
                "HOTP" -> OtpType.HOTP
                else -> OtpType.TOTP
            },
            algorithm = syncAccount.algorithm,
            digits = syncAccount.digits,
            period = syncAccount.period,
            counter = syncAccount.counter
        )
    }
    
    /**
     * 将账户列表转换为备份数据
     */
    fun createBackupData(accounts: List<OtpAccount>): OtpBackupData {
        return OtpBackupData(
            accounts = accounts.map { toSyncModel(it) }
        )
    }
    
    /**
     * 从备份数据中提取账户列表
     */
    fun extractAccountsFromBackup(backupData: OtpBackupData): List<OtpAccount> {
        return backupData.accounts.map { fromSyncModel(it) }
    }
}