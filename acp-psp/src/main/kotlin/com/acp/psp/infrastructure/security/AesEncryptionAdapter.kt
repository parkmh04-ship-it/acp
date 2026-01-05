package com.acp.psp.infrastructure.security

import com.acp.psp.application.port.output.EncryptionPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

@Component
class AesEncryptionAdapter(
    @Value("\${encryption.key:0123456789abcdef0123456789abcdef}") private val secretKey: String
) : EncryptionPort {

    private val ALGORITHM = "AES/GCM/NoPadding"
    private val TAG_LENGTH = 128
    private val IV_LENGTH = 12

    override fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec)
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        
        return Base64.getEncoder().encodeToString(combined)
    }

    override fun decrypt(cipherText: String): String {
        val combined = Base64.getDecoder().decode(cipherText)
        val iv = combined.copyOfRange(0, IV_LENGTH)
        val encrypted = combined.copyOfRange(IV_LENGTH, combined.size)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        val keySpec = SecretKeySpec(secretKey.toByteArray(), "AES")
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)
        val plainText = cipher.doFinal(encrypted)
        
        return String(plainText, Charsets.UTF_8)
    }
}
