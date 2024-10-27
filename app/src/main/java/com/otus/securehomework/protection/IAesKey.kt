package com.otus.securehomework.protection

import javax.crypto.SecretKey

interface IAesKey {
    fun clearKey()

    fun getSecretKey(): SecretKey
}