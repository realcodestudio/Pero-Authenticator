package com.rcbs.authenticator.repository

import com.rcbs.authenticator.data.OtpAccount
import com.rcbs.authenticator.data.OtpDao
import kotlinx.coroutines.flow.Flow

class OtpRepository(private val otpDao: OtpDao) {
    
    fun getAllAccounts(): Flow<List<OtpAccount>> = otpDao.getAllAccounts()
    
    suspend fun getAccountById(id: Long): OtpAccount? = otpDao.getAccountById(id)
    
    suspend fun insertAccount(account: OtpAccount): Long = otpDao.insertAccount(account)
    
    suspend fun updateAccount(account: OtpAccount) = otpDao.updateAccount(account)
    
    suspend fun deleteAccount(account: OtpAccount) = otpDao.deleteAccount(account)
    
    suspend fun deleteAccountById(id: Long) = otpDao.deleteAccountById(id)
}