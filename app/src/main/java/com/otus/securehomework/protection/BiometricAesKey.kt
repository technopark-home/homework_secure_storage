package com.otus.securehomework.protection

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class BiometricAesKey : AbstractAesKeyStore() {

    override fun clearKey() {
        removeKey(BIOMETRIC_AES_KEY_ALIAS)
    }

    override fun getSecretKey(): SecretKey {
        return keyStore.getKey(BIOMETRIC_AES_KEY_ALIAS, null) as? SecretKey ?: generateAesSecretKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateAesSecretKey(): SecretKey {
        return getKeyGenerator().generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenerator() = KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
        init(getKeyGenSpec())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getKeyGenSpec(): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
            BIOMETRIC_AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_LENGTH)
            .setRandomizedEncryptionRequired(false)
            .setUserAuthenticationRequired(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                }
            }
            .build()
    }

    companion object {
        const val BIOMETRIC_AES_KEY_ALIAS = "AES_OTUS_BIOMETRIC_DEMO"
    }
}
