package com.rcbs.wearotp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OtpDao {
    @Query("SELECT * FROM otp_accounts ORDER BY issuer, name")
    fun getAllAccounts(): Flow<List<OtpAccount>>

    @Query("SELECT * FROM otp_accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): OtpAccount?

    @Insert
    suspend fun insertAccount(account: OtpAccount): Long

    @Update
    suspend fun updateAccount(account: OtpAccount)

    @Delete
    suspend fun deleteAccount(account: OtpAccount)

    @Query("DELETE FROM otp_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)
}