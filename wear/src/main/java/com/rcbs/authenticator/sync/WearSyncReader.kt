package com.rcbs.authenticator.sync

import android.content.Context
import android.os.Environment
import com.rcbs.authenticator.data.WearOtpAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
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

class WearSyncReader(private val context: Context) {
    
    private val syncFileName = "wear_otp_sync.json"
    private val json = Json { 
        ignoreUnknownKeys = true
    }
    
    /**
     * 从共享存储读取手机导出的OTP数据
     */
    suspend fun readFromPhone(): Result<List<WearOtpAccount>> = withContext(Dispatchers.IO) {
        try {
            val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WearOTP")
            val syncFile = File(documentsDir, syncFileName)
            
            if (!syncFile.exists()) {
                return@withContext Result.success(emptyList())
            }
            
            val jsonString = syncFile.readText()
            val syncData = json.decodeFromString<WearSyncData>(jsonString)
            
            val wearAccounts = syncData.accounts.map { syncAccount ->
                WearOtpAccount(
                    id = syncAccount.id,
                    name = syncAccount.name,
                    issuer = syncAccount.issuer,
                    secret = syncAccount.secret,
                    algorithm = syncAccount.algorithm,
                    digits = syncAccount.digits,
                    period = syncAccount.period
                )
            }
            
            Result.success(wearAccounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检查同步文件是否存在
     */
    fun isSyncFileExists(): Boolean {
        val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WearOTP")
        return File(documentsDir, syncFileName).exists()
    }
    
    /**
     * 获取同步文件的最后修改时间
     */
    fun getSyncFileLastModified(): Long {
        val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WearOTP")
        val syncFile = File(documentsDir, syncFileName)
        return if (syncFile.exists()) syncFile.lastModified() else 0L
    }
}