package com.otus.securehomework.presentation.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.otus.securehomework.R
import com.otus.securehomework.data.Response
import com.otus.securehomework.data.dto.User
import com.otus.securehomework.databinding.FragmentLoginBinding
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
class LoginFragment : Fragment(R.layout.fragment_login) {

    @Inject
    lateinit var biometric : BiometricModule

    private lateinit var binding: FragmentLoginBinding
    private val viewModel by viewModels<AuthViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.progressbar.visible(false)
        binding.buttonLogin.enable(false)
        binding.useBiometrics.visible(false)
        binding.clearBiometrics.visible(false)

        viewModel.loginResponse.observe(viewLifecycleOwner) {
            binding.progressbar.visible(it is Response.Loading)
            when (it) {
                is Response.Success -> {
                    lifecycleScope.launch {
                        viewModel.saveAccessTokens(
                            it.value.user.access_token!!,
                            it.value.user.refresh_token!!
                        )
                        requireActivity().startNewActivity(HomeActivity::class.java)
                    }
                }
                is Response.Failure -> handleApiError(it) { login() }
                Response.Loading -> Unit
            }
        }
        binding.editTextTextPassword.addTextChangedListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            binding.buttonLogin.enable(email.isNotEmpty() && it.toString().isNotEmpty())
        }
        binding.buttonLogin.setOnClickListener {
            login()
        }

        if(biometric.isAvailable(requireContext())) {
            biometric.isUserRegistered().asLiveData().observe(viewLifecycleOwner) {
                    isUserRegistered ->
                binding.useBiometrics.visible(true)
                if(isUserRegistered) {
                    onUseBiometric()
                } else {
                    // Registration of biometric data
                    binding.clearBiometrics.visible(false)
                    binding.useBiometrics.setText(R.string.register_biometric)
                    binding.useBiometrics.setOnClickListener {
                        findNavController().navigate(R.id.action_loginFragment_to_registrationBiometricFragment)
                    }
                }
            }
        }
    }

    private fun login() {
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()
        viewModel.login(email, password)
    }

    private fun onUseBiometric() {
        binding.useBiometrics.setText(R.string.use_biometrics)
        val onAuthenticateUser = {
            ////context.getString(R.string.biometric_prompt_use_password_instead_text))
            biometric.authenticateUser(requireActivity(),
                getString(R.string.biometric_prompt_use_password_instead_text)) { userConnectionData: CharSequence ->
                val userData = Gson().fromJson(userConnectionData.toString(), User::class.java)
                lifecycleScope.launch {
                    viewModel.saveAccessTokens(
                        userData.access_token.toString(),
                        userData.refresh_token.toString()
                    )
                    requireActivity().startNewActivity(HomeActivity::class.java)
                }
            }
        }
        binding.useBiometrics.setOnClickListener {
            onAuthenticateUser()
        }
        onAuthenticateUser()
        binding.clearBiometrics.visible(true)
        binding.clearBiometrics.setOnClickListener {
            biometric.authenticateUser(requireActivity(), getString(R.string.cancel)) {
                    _: CharSequence ->
                lifecycleScope.launch {
                    biometric.clearRegisteredUser()
                }
            }
        }

    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}