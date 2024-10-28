package com.otus.securehomework.protection

import android.content.Context
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AesKeysSharedPreferences (
    private val applicationContext: Context,
    private val generatorParameterSpecRSA: IGeneratorParameterSpecRSA,
) : AbstractAesKeyStore() {
    companion object {
        private const val RSA_MODE_KEY_PROTECTION = "RSA/ECB/PKCS1Padding"
        private const val ENCRYPTED_KEY_NAME = "RSAEncryptedKeysKeyName"
        private const val RSA_ALGORITHM = "RSA"
        private const val SHARED_PREFERENCE_NAME = "RSAEncryptedKeysSharedPreferences"
    }

    private val sharedPreferences by lazy {
        applicationContext.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    override fun getSecretKey(): SecretKey {
        return getAesSecretKeyPreferences() ?: generateAesSecretKey()
    }

    override fun clearKey() {
        sharedPreferences.edit().apply {
            remove(ENCRYPTED_KEY_NAME)
            commit()
        }
    }

    private fun getAesSecretKeyPreferences(): SecretKey? {
        val encryptedKeyBase64Encoded = getSecretKeyFromSharedPreferences()
        return encryptedKeyBase64Encoded?.let {
            val encryptedKey = Base64.decode(encryptedKeyBase64Encoded, Base64.DEFAULT)
            val key = rsaDecryptKey(encryptedKey)
            SecretKeySpec(key, AES_ALGORITHM)
        }
    }

    private fun rsaDecryptKey(encryptedKey: ByteArray?): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE_KEY_PROTECTION)
        cipher.init(Cipher.DECRYPT_MODE, getRsaPrivateKey())
        return cipher.doFinal(encryptedKey)
    }

    private fun getSecretKeyFromSharedPreferences(): String? {
        return sharedPreferences.getString(ENCRYPTED_KEY_NAME, null)
    }

    private fun generateAesSecretKey(): SecretKey {
        return generateAndSaveAesSecretKeyLessThanM()
    }

    private fun generateAndSaveAesSecretKeyLessThanM(): SecretKey {
        val key = ByteArray(16)
        SecureRandom().run {
            nextBytes(key)
        }
        val encryptedKeyBase64encoded = Base64.encodeToString(
            rsaEncryptKey(key),
            Base64.DEFAULT
        )
        sharedPreferences.edit().apply {
            putString(ENCRYPTED_KEY_NAME, encryptedKeyBase64encoded)
            apply()
        }

        return SecretKeySpec(key, AES_ALGORITHM)
    }

    private fun rsaEncryptKey(secret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(RSA_MODE_KEY_PROTECTION)
        cipher.init(Cipher.ENCRYPT_MODE, getRsaPublicKey())
        return cipher.doFinal(secret)
    }

    private fun getRsaPrivateKey(): PrivateKey {
        return keyStore.getKey(IGeneratorParameterSpecRSA.RSA_KEY_ALIAS, null) as? PrivateKey ?: generateRsaSecretKey().private
    }

    private fun getRsaPublicKey(): PublicKey {
        return keyStore.getCertificate(IGeneratorParameterSpecRSA.RSA_KEY_ALIAS)?.publicKey?: generateRsaSecretKey().public
    }

    private fun generateRsaSecretKey(): KeyPair {
        return KeyPairGenerator.getInstance(RSA_ALGORITHM, KEY_PROVIDER).run {
            initialize(generatorParameterSpecRSA.getAlgorithmParameterSpec())
            generateKeyPair()
        }
    }
}