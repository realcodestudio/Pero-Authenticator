package com.rcbs.authenticator.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.rcbs.authenticator.data.OtpAccount
import com.rcbs.authenticator.repository.OtpRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class BackupAccount(
    val name: String,
    val issuer: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val type: String = "TOTP"
)

@Serializable
data class BackupData(
    val version: String = "1.0",
    val timestamp: Long = System.currentTimeMillis(),
    val accounts: List<BackupAccount>
)

@Serializable
data class BackupInfo(
    val version: String,
    val timestamp: Long,
    val accountCount: Int
)

/**
 * 简化的备份管理器
 */
class SimpleBackupManager(
    private val context: Context,
    private val repository: OtpRepository
) {
    companion object {
        private const val TAG = "SimpleBackupManager"
    }

    /**
     * 导出备份到文件
     */
    suspend fun exportBackup(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val accounts = repository.getAllAccounts().first()
            val backupAccounts = accounts.map { account ->
                BackupAccount(
                    name = account.name,
                    issuer = account.issuer,
                    secret = account.secret,
                    algorithm = account.algorithm,
                    digits = account.digits,
                    period = account.period,
                    type = account.type.name
                )
            }
            
            val backupData = BackupData(accounts = backupAccounts)
            val json = Json { prettyPrint = true }
            val jsonString = json.encodeToString(backupData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
                outputStream.flush()
            } ?: throw IOException("无法打开输出流")
            
            Log.d(TAG, "备份导出成功: ${accounts.size} 个账户")
            Result.success("成功导出 ${accounts.size} 个账户到备份文件")
        } catch (e: Exception) {
            Log.e(TAG, "备份导出失败", e)
            Result.failure(e)
        }
    }

    /**
     * 验证备份文件
     */
    suspend fun validateBackupFile(uri: Uri): Result<BackupInfo> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: throw IOException("无法读取文件")
            
            val backupData = Json.decodeFromString<BackupData>(jsonString)
            val backupInfo = BackupInfo(
                version = backupData.version,
                timestamp = backupData.timestamp,
                accountCount = backupData.accounts.size
            )
            
            Log.d(TAG, "备份文件验证成功: ${backupInfo.accountCount} 个账户")
            Result.success(backupInfo)
        } catch (e: Exception) {
            Log.e(TAG, "备份文件验证失败", e)
            Result.failure(e)
        }
    }

    /**
     * 从备份文件导入
     */
    suspend fun importBackup(uri: Uri, replaceExisting: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: throw IOException("无法读取文件")
            
            val backupData = Json.decodeFromString<BackupData>(jsonString)
            
            if (replaceExisting) {
                // 删除所有现有账户
                val existingAccounts = repository.getAllAccounts().first()
                for (account in existingAccounts) {
                    repository.deleteAccount(account)
                }
            }
            
            // 添加备份中的账户
            var importedCount = 0
            for (backupAccount in backupData.accounts) {
                val otpAccount = OtpAccount(
                    name = backupAccount.name,
                    issuer = backupAccount.issuer,
                    secret = backupAccount.secret,
                    algorithm = backupAccount.algorithm,
                    digits = backupAccount.digits,
                    period = backupAccount.period,
                    type = when (backupAccount.type) {
                        "HOTP" -> com.rcbs.authenticator.data.OtpType.HOTP
                        else -> com.rcbs.authenticator.data.OtpType.TOTP
                    }
                )
                
                // 检查是否已存在相同的账户（基于名称和发行商）
                val allAccounts = repository.getAllAccounts().first()
                val existing = allAccounts.find { account ->
                    account.name == otpAccount.name && account.issuer == otpAccount.issuer 
                }
                
                if (existing == null || replaceExisting) {
                    repository.insertAccount(otpAccount)
                    importedCount++
                }
            }
            
            val message = if (replaceExisting) {
                "成功替换导入 $importedCount 个账户"
            } else {
                "成功合并导入 $importedCount 个新账户"
            }
            
            Log.d(TAG, message)
            Result.success(message)
        } catch (e: Exception) {
            Log.e(TAG, "备份导入失败", e)
            Result.failure(e)
        }
    }

    /**
     * 创建快速备份到内部存储
     */
    suspend fun createQuickBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val accounts = repository.getAllAccounts().first()
            val backupAccounts = accounts.map { account ->
                BackupAccount(
                    name = account.name,
                    issuer = account.issuer,
                    secret = account.secret,
                    algorithm = account.algorithm,
                    digits = account.digits,
                    period = account.period,
                    type = account.type.name
                )
            }
            
            val backupData = BackupData(accounts = backupAccounts)
            val json = Json { prettyPrint = true }
            val jsonString = json.encodeToString(backupData)
            
            // 保存到内部存储
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "WearOTP_QuickBackup_${dateFormat.format(Date())}.otpbackup"
            val file = context.getFileStreamPath(fileName)
            
            file.writeText(jsonString, Charsets.UTF_8)
            
            Log.d(TAG, "快速备份创建成功: ${accounts.size} 个账户")
            Result.success("快速备份已保存到: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "快速备份创建失败", e)
            Result.failure(e)
        }
    }

    /**
     * 生成备份文件名
     */
    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "WearOTP_Backup_${dateFormat.format(Date())}.otpbackup"
    }
}