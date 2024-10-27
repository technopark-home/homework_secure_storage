package com.otus.securehomework.protection

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


@RequiresApi(Build.VERSION_CODES.M)
class AesKey : AbstractAesKeyStore() {

    override fun getSecretKey(): SecretKey {
        return keyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: generateAesSecretKey()
    }

    override fun clearKey() {
        removeKey(AES_KEY_ALIAS)
    }

    private fun generateAesSecretKey(): SecretKey {
        return getKeyGenerator().generateKey()
    }

    private fun getKeyGenerator() = KeyGenerator.getInstance(AES_ALGORITHM, KEY_PROVIDER).apply {
        init(getKeyGenSpec())
    }

    private fun getKeyGenSpec(): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
            AES_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(false)
            .setKeySize(KEY_LENGTH)
            .build()
    }

    companion object {
        const val AES_KEY_ALIAS = "AES_OTUS_DEMO"
    }
}