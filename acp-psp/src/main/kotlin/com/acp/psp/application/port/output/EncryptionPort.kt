package com.acp.psp.application.port.output

/**
 * 데이터 암호화 및 복호화를 담당하는 포트
 */
interface EncryptionPort {
    /** 데이터를 암호화합니다. */
    fun encrypt(plainText: String): String
    
    /** 암호화된 데이터를 복호화합니다. */
    fun decrypt(cipherText: String): String
}
