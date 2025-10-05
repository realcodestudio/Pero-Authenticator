package com.rcbs.authenticator.data

data class WearOtpAccount(
    val id: Long,
    val name: String,
    val issuer: String,
    val secret: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30
)

data class WearOtpCode(
    val accountId: Long,
    val code: String,
    val remainingTime: Long
)