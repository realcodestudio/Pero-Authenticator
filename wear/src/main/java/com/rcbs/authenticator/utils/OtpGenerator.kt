package com.rcbs.authenticator.utils

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
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
}