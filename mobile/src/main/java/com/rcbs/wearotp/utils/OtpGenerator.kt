package com.rcbs.wearotp.utils

import android.net.Uri
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
            val cleanSecret = secret.replace(" ", "").uppercase()
            base32.decode(cleanSecret)
            true
        } catch (e: Exception) {
            android.util.Log.e("OtpGenerator", "Invalid secret format: $secret", e)
            false
        }
    }

    /**
     * 创建GitHub格式的测试URI
     */
    fun createGitHubTestUri(setupKey: String, username: String = "testuser"): String {
        return "otpauth://totp/GitHub%3A$username?secret=$setupKey&issuer=GitHub&algorithm=SHA1&digits=6&period=30"
    }
    
    /**
     * 从otpauth URI解析参数 - 使用Android Uri类进行更可靠的解析
     */
    fun parseOtpAuthUri(uri: String): OtpAuthData? {
        try {
            android.util.Log.d("OtpGenerator", "开始解析URI: $uri")
            
            if (!uri.startsWith("otpauth://")) {
                android.util.Log.e("OtpGenerator", "URI不是以otpauth://开头")
                return null
            }

            val androidUri = Uri.parse(uri)
            val type = androidUri.authority // totp 或 hotp
            val path = androidUri.path?.removePrefix("/") ?: ""
            
            android.util.Log.d("OtpGenerator", "解析结果: type=$type, path=$path")
            
            // 解析标签 - GitHub格式: GitHub:username
            var issuer = ""
            var account = ""
            
            if (path.isNotEmpty()) {
                // 处理URL编码的冒号
                val decodedPath = java.net.URLDecoder.decode(path, "UTF-8")
                android.util.Log.d("OtpGenerator", "解码后的路径: $decodedPath")
                
                val colonIndex = decodedPath.indexOf(':')
                if (colonIndex > 0) {
                    // 找到冒号，分割issuer和account
                    issuer = decodedPath.substring(0, colonIndex).trim()
                    account = decodedPath.substring(colonIndex + 1).trim()
                } else {
                    // 没有冒号，整个路径作为account
                    account = decodedPath.trim()
                }
            }

            android.util.Log.d("OtpGenerator", "从路径解析得到: issuer='$issuer', account='$account'")

            // 获取查询参数
            val secret = androidUri.getQueryParameter("secret")
            if (secret.isNullOrEmpty()) {
                android.util.Log.e("OtpGenerator", "缺少secret参数")
                return null
            }
            
            android.util.Log.d("OtpGenerator", "Secret: $secret")
            
            // 验证secret格式
            if (!isValidSecret(secret)) {
                android.util.Log.e("OtpGenerator", "无效的secret格式: $secret")
                return null
            }
            
            // 如果查询参数中有issuer，优先使用
            val queryIssuer = androidUri.getQueryParameter("issuer")
            val finalIssuer = if (!queryIssuer.isNullOrEmpty()) queryIssuer else issuer
            
            val algorithm = androidUri.getQueryParameter("algorithm") ?: "SHA1"
            val digits = androidUri.getQueryParameter("digits")?.toIntOrNull() ?: 6
            val period = androidUri.getQueryParameter("period")?.toIntOrNull() ?: 30

            android.util.Log.d("OtpGenerator", "参数: issuer='$finalIssuer', algorithm=$algorithm, digits=$digits, period=$period")

            // 验证参数范围
            if (digits < 4 || digits > 10) {
                android.util.Log.e("OtpGenerator", "无效的digits值: $digits")
                return null
            }
            
            if (period < 1 || period > 300) {
                android.util.Log.e("OtpGenerator", "无效的period值: $period")
                return null
            }

            val result = OtpAuthData(
                type = type ?: "totp",
                account = account,
                issuer = finalIssuer,
                secret = secret,
                algorithm = algorithm,
                digits = digits,
                period = period
            )
            
            android.util.Log.d("OtpGenerator", "解析成功: $result")
            return result
        } catch (e: Exception) {
            android.util.Log.e("OtpGenerator", "解析URI时发生异常", e)
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