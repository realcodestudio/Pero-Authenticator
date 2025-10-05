package com.rcbs.wearotp.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wear OS 数据同步服务
 */
class WearSyncService(private val context: Context) {
    
    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)
    
    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    companion object {
        private const val TAG = "WearSyncService"
        private const val SYNC_PATH = "/otp_sync"
        private const val BACKUP_PATH = "/otp_backup"
        private const val CAPABILITY_NAME = "otp_sync"
    }
    
    /**
     * 检查手表连接状态
     */
    suspend fun checkWearConnection(): Boolean {
        return try {
            val nodes = capabilityClient.getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE).await()
            val connected = nodes.capabilityInfo.nodes.isNotEmpty()
            _syncStatus.value = _syncStatus.value.copy(isConnected = connected)
            connected
        } catch (e: Exception) {
            Log.e(TAG, "检查手表连接失败", e)
            _syncStatus.value = _syncStatus.value.copy(isConnected = false, error = e.message)
            false
        }
    }
    
    /**
     * 发送同步消息到手表
     */
    suspend fun sendSyncMessage(message: SyncMessage): Boolean {
        return try {
            _syncStatus.value = _syncStatus.value.copy(syncInProgress = true)
            
            val nodes = capabilityClient.getCapability(CAPABILITY_NAME, CapabilityClient.FILTER_REACHABLE).await()
            val messageData = Json.encodeToString(message).toByteArray()
            
            var success = false
            for (node in nodes.capabilityInfo.nodes) {
                try {
                    messageClient.sendMessage(node.id, SYNC_PATH, messageData).await()
                    success = true
                    Log.d(TAG, "同步消息发送成功到节点: ${node.displayName}")
                } catch (e: Exception) {
                    Log.e(TAG, "发送消息到节点 ${node.displayName} 失败", e)
                }
            }
            
            _syncStatus.value = _syncStatus.value.copy(
                syncInProgress = false,
                lastSyncTime = if (success) System.currentTimeMillis() else _syncStatus.value.lastSyncTime,
                error = if (success) null else "发送同步消息失败"
            )
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "发送同步消息失败", e)
            _syncStatus.value = _syncStatus.value.copy(
                syncInProgress = false,
                error = e.message
            )
            false
        }
    }
    
    /**
     * 发送完整数据同步
     */
    suspend fun sendFullSync(backupData: OtpBackupData): Boolean {
        val message = SyncMessage(
            type = SyncMessageType.FULL_SYNC,
            data = Json.encodeToString(backupData)
        )
        return sendSyncMessage(message)
    }
    
    /**
     * 发送单个账户添加
     */
    suspend fun sendAccountAdd(account: SyncOtpAccount): Boolean {
        val message = SyncMessage(
            type = SyncMessageType.ADD_ACCOUNT,
            data = Json.encodeToString(account)
        )
        return sendSyncMessage(message)
    }
    
    /**
     * 发送账户更新
     */
    suspend fun sendAccountUpdate(account: SyncOtpAccount): Boolean {
        val message = SyncMessage(
            type = SyncMessageType.UPDATE_ACCOUNT,
            data = Json.encodeToString(account)
        )
        return sendSyncMessage(message)
    }
    
    /**
     * 发送账户删除
     */
    suspend fun sendAccountDelete(accountId: Long): Boolean {
        val message = SyncMessage(
            type = SyncMessageType.DELETE_ACCOUNT,
            data = accountId.toString()
        )
        return sendSyncMessage(message)
    }
    
    /**
     * 请求手表备份数据
     */
    suspend fun requestWearBackup(): Boolean {
        val message = SyncMessage(
            type = SyncMessageType.BACKUP_REQUEST,
            data = ""
        )
        return sendSyncMessage(message)
    }
}