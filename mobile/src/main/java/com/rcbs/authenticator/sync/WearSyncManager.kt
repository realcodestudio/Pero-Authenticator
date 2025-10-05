package com.rcbs.authenticator.sync

import android.content.Context
import android.os.Environment
import com.rcbs.authenticator.data.OtpAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class WearSyncData(
    val accounts: List<SyncAccount>,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SyncAccount(
    val id: Long,
    val name: String,
    val issuer: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
)

class WearSyncManager(private val context: Context) {
    
    private val syncFileName = "wear_otp_sync.json"
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * 导出OTP数据到共享存储，供手表读取
     */
    suspend fun exportToWear(accounts: List<OtpAccount>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val syncAccounts = accounts.map { account ->
                SyncAccount(
                    id = account.id,
                    name = account.name,
                    issuer = account.issuer,
                    secret = account.secret,
                    algorithm = account.algorithm,
                    digits = account.digits,
                    period = account.period
                )
            }
            
            val syncData = WearSyncData(accounts = syncAccounts)
            val jsonString = json.encodeToString(syncData)
            
            // 写入到外部存储的Documents目录
            val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WearOTP")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            
            val syncFile = File(documentsDir, syncFileName)
            syncFile.writeText(jsonString)
            
            Result.success("同步数据已导出到: ${syncFile.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取同步文件路径
     */
    fun getSyncFilePath(): String {
        val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WearOTP")
        return File(documentsDir, syncFileName).absolutePath
    }
    
    /**
     * 检查同步文件是否存在
     */
    fun isSyncFileExists(): Boolean {
        val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WearOTP")
        return File(documentsDir, syncFileName).exists()
    }
}