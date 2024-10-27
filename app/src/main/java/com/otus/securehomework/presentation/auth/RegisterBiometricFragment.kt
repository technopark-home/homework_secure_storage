package com.otus.securehomework.presentation.auth

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.databinding.FragmentRegisterBiomericBinding
import com.otus.securehomework.presentation.enable
import com.otus.securehomework.presentation.handleApiError
import com.otus.securehomework.presentation.home.HomeActivity
import com.otus.securehomework.presentation.startNewActivity
import com.otus.securehomework.presentation.visible
import com.otus.securehomework.protection.BiometricModule
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegisterBiometricFragment : Fragment(R.layout.fragment_register_biomeric) {
    @Inject
    lateinit var biometric : BiometricModule
    private lateinit var binding: FragmentRegisterBiomericBinding
    private val viewModel by viewModels<AuthViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegisterBiomericBinding.bind(view)
        binding.progressbar.visible(false)
        binding.buttonRegister.enable(false)
        binding.editTextTextPassword.addTextChangedListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            binding.buttonRegister.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }
        binding.buttonRegister.setOnClickListener {
            login()
        }
        binding.cancelRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_registrationBiometricFragment_to_loginFragment)
        }
        viewModel.loginResponse.observe(viewLifecycleOwner) {
            binding.progressbar.visible(it is Response.Loading)
            when (it) {
                is Response.Success -> {
                    val userConnectionData = it.value.user
                    val json = Gson().toJson(userConnectionData)
                    biometric.registerUserBiometrics(
                        context = requireActivity(),
                        onSuccess = { requireActivity().startNewActivity(HomeActivity::class.java) },
                        userData = json
                    )

                    lifecycleScope.launch {
                        viewModel.saveAccessTokens(
                            it.value.user.access_token!!,
                            it.value.user.refresh_token!!
                        )
                    }
                }
                is Response.Failure -> handleApiError(it) { login() }
                Response.Loading -> Unit
            }
        }
    }

    private fun login() {
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()
        viewModel.login(email, password)
    }

    /*@RequiresApi(Build.VERSION_CODES.P)
    private fun showBiometricPromptForEncryption( onSuccess: (authResult: BiometricPrompt. AuthenticationResult) -> Unit = {}) {
        val canAuthenticate = BiometricManager.from(requireContext()).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            //val secretKeyName = getString(R.string.secret_key_name)
            //var cryptographyManager = CryptoManager()
            //val cipher = cryptographyManager.initEncryptionCipher(secretKeyName)
            val biometricPrompt =
                BiometricHelper.registerUserBiometrics(requireActivity(), onSuccess)
            //val promptInfo = BiometricHelper.getPromptInfo(this)
            //biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }*/

    /*private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            SampleAppUser.fakeToken?.let { token ->
                //Log.d(TAG, "The token from server is $token")
                val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
                cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                    encryptedServerTokenWrapper,
                    applicationContext,
                    SHARED_PREFS_FILENAME,
                    Context.MODE_PRIVATE,
                    CIPHERTEXT_WRAPPER
                )
            }
        }
    }*/

}