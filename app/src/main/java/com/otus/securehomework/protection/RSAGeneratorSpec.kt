package com.otus.securehomework.protection

import android.content.Context
import android.security.KeyPairGeneratorSpec
import java.math.BigInteger
import java.security.spec.AlgorithmParameterSpec
import java.util.Calendar
import javax.security.auth.x500.X500Principal

class RSAGeneratorSpecImpl (
    private val applicationContext: Context
) : IGeneratorParameterSpecRSA {
    override fun getAlgorithmParameterSpec(): AlgorithmParameterSpec {
        val start: Calendar = Calendar.getInstance()
        val end: Calendar = Calendar.getInstance()
        end.add(Calendar.YEAR, 30)
        return KeyPairGeneratorSpec.Builder(applicationContext)
            .setAlias(IGeneratorParameterSpecRSA.RSA_KEY_ALIAS)
            .setSubject(X500Principal("CN=${IGeneratorParameterSpecRSA.RSA_KEY_ALIAS}"))
            .setSerialNumber(BigInteger.TEN)
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()
    }
}