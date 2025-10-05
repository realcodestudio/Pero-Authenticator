package com.rcbs.authenticator.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import java.nio.charset.StandardCharsets

/**
 * 密码管理器 - 用于应用内密码锁定功能
 */
class PasswordManager(private val context: Context) {
    
    companion object {
        private const val KEY_ALIAS = "WearOTPPasswordKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val PREF_NAME = "password_prefs"
        private const val KEY_ENCRYPTED_PASSWORD = "encrypted_password"
        private const val KEY_IV = "password_iv"
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    init {
        generateKeyIfNeeded()
    }
    
    /**
     * 生成加密密钥（如果不存在）
     */
    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    /**
     * 设置应用密码
     */
    fun setPassword(password: String): Boolean {
        return try {
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val encryptedPassword = cipher.doFinal(password.toByteArray(StandardCharsets.UTF_8))
            val iv = cipher.iv
            
            prefs.edit()
                .putString(KEY_ENCRYPTED_PASSWORD, Base64.encodeToString(encryptedPassword, Base64.DEFAULT))
                .putString(KEY_IV, Base64.encodeToString(iv, Base64.DEFAULT))
                .apply()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 验证密码
     */
    fun verifyPassword(inputPassword: String): Boolean {
        return try {
            val encryptedPasswordStr = prefs.getString(KEY_ENCRYPTED_PASSWORD, null)
            val ivStr = prefs.getString(KEY_IV, null)
            
            if (encryptedPasswordStr == null || ivStr == null) {
                return false
            }
            
            val encryptedPassword = Base64.decode(encryptedPasswordStr, Base64.DEFAULT)
            val iv = Base64.decode(ivStr, Base64.DEFAULT)
            
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            
            val decryptedPassword = String(cipher.doFinal(encryptedPassword), StandardCharsets.UTF_8)
            decryptedPassword == inputPassword
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查是否已设置密码
     */
    fun hasPassword(): Boolean {
        return prefs.contains(KEY_ENCRYPTED_PASSWORD) && prefs.contains(KEY_IV)
    }
    
    /**
     * 清除密码
     */
    fun clearPassword(): Boolean {
        return try {
            prefs.edit()
                .remove(KEY_ENCRYPTED_PASSWORD)
                .remove(KEY_IV)
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 验证密码强度
     */
    fun validatePasswordStrength(password: String): PasswordStrength {
        return when {
            password.isEmpty() -> PasswordStrength.EMPTY
            password.length < 4 -> PasswordStrength.TOO_SHORT
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 8 -> PasswordStrength.MEDIUM
            password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() } -> PasswordStrength.STRONG
            else -> PasswordStrength.MEDIUM
        }
    }
    
    /**
     * 获取密码强度描述
     */
    fun getPasswordStrengthDescription(strength: PasswordStrength): String {
        return when (strength) {
            PasswordStrength.EMPTY -> "请输入密码"
            PasswordStrength.TOO_SHORT -> "密码太短，至少需要4位"
            PasswordStrength.WEAK -> "密码强度较弱"
            PasswordStrength.MEDIUM -> "密码强度中等"
            PasswordStrength.STRONG -> "密码强度很强"
        }
    }
}

/**
 * 密码强度枚举
 */
enum class PasswordStrength {
    EMPTY,
    TOO_SHORT,
    WEAK,
    MEDIUM,
    STRONG
}