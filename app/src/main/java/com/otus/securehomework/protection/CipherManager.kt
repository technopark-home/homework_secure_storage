package com.otus.securehomework.protection

import com.google.gson.Gson
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class CipherManager @Inject constructor (
    private val cryptoKey: IAesKey
)  {

    fun initEncryptionCipher(): Cipher {
        val key = cryptoKey.getSecretKey()
        return Cipher.getInstance(AES_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key, getParameterSpec())
        }
    }

    fun initDecryptionCipher(initializationVector: ByteArray): Cipher {
        val key = cryptoKey.getSecretKey()
        return Cipher.getInstance(AES_TRANSFORMATION).apply {
            init( Cipher.DECRYPT_MODE, key, getParameterSpec(initializationVector))
        }
    }

    fun encrypt(plainText: CharSequence, cipher: Cipher): CharSequence {
        val encodedBytes = cipher.doFinal( charSequenceToByteArray(plainText) )
        return Gson().toJson( AesPayload(cipher.iv, encodedBytes) )
    }

    fun decrypt(cipherText: ByteArray, cipher: Cipher): CharSequence {
        val decoded = cipher.doFinal(cipherText)
        return String(decoded, Charsets.UTF_8)
    }

    fun getPayload(encrypted: CharSequence): AesPayload =
        Gson().fromJson(encrypted.toString(), AesPayload::class.java)

    fun encrypt(plainText: CharSequence): CharSequence {
        return encrypt(plainText, initEncryptionCipher())
    }

    private fun getParameterSpec(iv: ByteArray = byteArrayOf()): AlgorithmParameterSpec {
        val secIV = ByteArray(GCM_IV_LENGTH)
        if(iv.isEmpty()) {
            SecureRandom().nextBytes(secIV)
        } else {
            iv.copyInto( destination = secIV, endIndex = iv.size )
        }
        return GCMParameterSpec(GCM_TAG_LEN, secIV)
    }

    fun decrypt(encrypted: CharSequence): CharSequence {
            val encryptedPayload = Gson().fromJson(encrypted.toString(), AesPayload::class.java)
            return decrypt( encryptedPayload.ciphertext, initDecryptionCipher( encryptedPayload.iv ))
    }

    private fun charSequenceToByteArray(charSequence: CharSequence): ByteArray = charSequence.map { it.code.toByte() }.toByteArray()

    private companion object {
        private const val GCM_TAG_LEN = 128
        private const val GCM_IV_LENGTH = 12
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    }
}

class AesPayload(val iv: ByteArray, val ciphertext: ByteArray)