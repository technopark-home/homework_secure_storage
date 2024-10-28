package com.otus.securehomework.protection

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class RSAGenEncryptSpecImpl : IGeneratorParameterSpecRSA {

    override fun getAlgorithmParameterSpec() = KeyGenParameterSpec.Builder(
        IGeneratorParameterSpecRSA.RSA_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        .setUserAuthenticationRequired(true)
        .setRandomizedEncryptionRequired(false)
        .build()
}