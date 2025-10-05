package com.rcbs.wearotp.utils

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object OtpGenerator {
    
    /**
     * 生成TOTP (Time-based One-Time Password)
     */
    fun generateTOTP(
        secret: String,
        timeStepSeconds: Long = 30,
        digits: Int = 6,
        algorithm: String = "HmacSHA1"
    ): String {
        val timeStep = System.currentTimeMillis() / 1000 / timeStepSeconds
        return generateHOTP(secret, timeStep, digits, algorithm)
    }

    /**
     * 生成HOTP (HMAC-based One-Time Password)
     */
    fun generateHOTP(
        secret: String,
        counter: Long,
        digits: Int = 6,
        algorithm: String = "HmacSHA1"
    ): String {
        try {
            // 解码Base32密钥
            val base32 = Base32()
            val key = base32.decode(secret.uppercase())

            // 创建HMAC
            val mac = Mac.getInstance(algorithm)
            val keySpec = SecretKeySpec(key, algorithm)
            mac.init(keySpec)

            // 计算HMAC
            val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()
            val hash = mac.doFinal(counterBytes)

            // 动态截断
            val offset = hash[hash.size - 1].toInt() and 0x0F
            val truncatedHash = ByteBuffer.wrap(hash, offset, 4).int and 0x7FFFFFFF

            // 生成OTP
            val otp = truncatedHash % 10.0.pow(digits).toInt()
            return String.format("%0${digits}d", otp)
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate OTP", e)
        }
    }

    /**
     * 获取当前TOTP的剩余时间（秒）
     */
    fun getRemainingTime(timeStepSeconds: Long = 30): Long {
        return timeStepSeconds - (System.currentTimeMillis() / 1000) % timeStepSeconds
    }

    /**
     * 验证密钥格式
     */
    fun isValidSecret(secret: String): Boolean {
        return try {
            val base32 = Base32()
            base32.decode(secret.uppercase())
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 从otpauth URI解析参数
     */
    fun parseOtpAuthUri(uri: String): OtpAuthData? {
        try {
            if (!uri.startsWith("otpauth://")) return null

            val url = java.net.URL(uri)
            val type = url.host // totp 或 hotp
            val path = url.path.removePrefix("/")
            
            // 解析标签 (issuer:account 或 account)
            val parts = path.split(":")
            val issuer = if (parts.size > 1) parts[0] else ""
            val account = if (parts.size > 1) parts[1] else parts[0]

            // 解析查询参数
            val params = url.query?.split("&")?.associate { param ->
                val (key, value) = param.split("=", limit = 2)
                key to java.net.URLDecoder.decode(value, "UTF-8")
            } ?: emptyMap()

            val secret = params["secret"] ?: return null
            val algorithm = params["algorithm"] ?: "SHA1"
            val digits = params["digits"]?.toIntOrNull() ?: 6
            val period = params["period"]?.toIntOrNull() ?: 30

            return OtpAuthData(
                type = type,
                account = account,
                issuer = issuer,
                secret = secret,
                algorithm = algorithm,
                digits = digits,
                period = period
            )
        } catch (e: Exception) {
            return null
        }
    }
}

data class OtpAuthData(
    val type: String,
    val account: String,
    val issuer: String,
    val secret: String,
    val algorithm: String,
    val digits: Int,
    val period: Int
)