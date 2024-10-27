package com.otus.securehomework.protection

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.otus.securehomework.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

const val CIPHER_AES_BIOMETRIC = "preferences_user_biometric_data "

class BiometricModule @Inject constructor(
    private val biometricPreferences: BiometricPreferences,
    @Named(CIPHER_AES_BIOMETRIC) private val cryptoManager: CipherManager
) {
    private fun isBiometricAvailable(): Boolean = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)

    fun isAvailable(context: Context): Boolean = try {
        if( isBiometricAvailable() ) {
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )

            (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
        } else
            false
    } catch(_: Exception) {
        false
    }

    private fun isBiometricStrongAvailable(context: Context): Boolean = try {
        if( isBiometricAvailable() ) {
            (BiometricManager
                .from(context)
                .canAuthenticate( BiometricManager.Authenticators.BIOMETRIC_STRONG )
                == BiometricManager.BIOMETRIC_SUCCESS )
        } else
            false
    } catch(_: Exception) {
        false
    }

    private fun isBiometricWeakAvailable(context: Context): Boolean = try {
        if( isBiometricAvailable() ) {
            (BiometricManager
                .from(context)
                .canAuthenticate( BiometricManager.Authenticators.BIOMETRIC_WEAK )
                    == BiometricManager.BIOMETRIC_SUCCESS )
        } else
            false
    } catch(_: Exception) {
        false
    }

    fun isUserRegistered() : Flow<Boolean> {
        return biometricPreferences.isBiometricEnabled()
    }

    suspend fun clearRegisteredUser() {
        return biometricPreferences.clearBiometric()
    }

    fun registerUserBiometrics(
        context: FragmentActivity,
        onSuccess: () -> Unit = {},
        userData: CharSequence,
    ) {
        if(isBiometricAvailable()) {
            val cipher = cryptoManager.initEncryptionCipher()
            val biometricPrompt = getBiometricPrompt(context) { authResult ->
                authResult.cryptoObject?.cipher?.let { cipher ->
                    context.lifecycleScope.launch {
                        val encryptUserData = cryptoManager.encrypt(userData, cipher)
                        biometricPreferences.setStoreUserData(encryptUserData)
                    }
                    onSuccess()
                }
            }
            biometricPrompt.authenticate(
                getPromptInfoRegister(context),
                BiometricPrompt.CryptoObject(cipher)
            )
        }
    }

    private fun getPromptInfoAuthentication(context: FragmentActivity,
                                            negativeButtonText: String): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_prompt_title_text))
            .setDescription(context.getString(R.string.biometric_prompt_description_text))
            .setConfirmationRequired(false)
            .setNegativeButtonText(negativeButtonText)
            .apply {
                if(isBiometricStrongAvailable(context))
                    setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                else if(isBiometricWeakAvailable(context))
                    setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            }
            .build()
    }
    
    fun authenticateUser(
        context: FragmentActivity,
        negativeButtonText: String,
        onSuccess: (plainText: CharSequence) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.lifecycleScope.launch {
                    val userData = biometricPreferences.getStoreUserData()
                    val payloadData = cryptoManager.getPayload(userData)
                    val cipher = cryptoManager.initDecryptionCipher(payloadData.iv)
                    val biometricPrompt = getBiometricPrompt(context) { authResult ->
                        authResult.cryptoObject?.cipher?.let { cipherAuth ->
                            val userDataDecrypt =
                                cryptoManager.decrypt(payloadData.ciphertext, cipherAuth)
                            onSuccess(userDataDecrypt)
                        }
                    }
                    val promptInfo = getPromptInfoAuthentication(context, negativeButtonText)
                    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    private fun getPromptInfoRegister(context: FragmentActivity): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.registration_biometric))
            .setSubtitle(context.getString(R.string.authenticate_using_your_biometric_credential))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()
    }

    private fun getBiometricPrompt(
        context: FragmentActivity,
        onAuthSucceed: (BiometricPrompt.AuthenticationResult) -> Unit
    ): BiometricPrompt {
        val biometricPrompt =
            BiometricPrompt(
                context,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        Log.e("TAG", "Authentication Succeeded: ${result.cryptoObject}")
                        onAuthSucceed(result)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        Log.e("TAG", "onAuthenticationError")
                    }

                    override fun onAuthenticationFailed() {
                        Log.e("TAG", "onAuthenticationFailed")
                    }
                }
            )
        return biometricPrompt
    }
}