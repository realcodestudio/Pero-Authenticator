package com.rcbs.wearotp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "otp_accounts")
data class OtpAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val issuer: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val type: OtpType = OtpType.TOTP
)

enum class OtpType {
    TOTP, // Time-based OTP
    HOTP  // Counter-based OTP
}