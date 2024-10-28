package com.otus.securehomework.protection

import java.security.spec.AlgorithmParameterSpec

interface IGeneratorParameterSpecRSA {

    fun getAlgorithmParameterSpec() : AlgorithmParameterSpec

    companion object {
        const val RSA_KEY_ALIAS = "RSA_OTUS_DEMO"
    }
}