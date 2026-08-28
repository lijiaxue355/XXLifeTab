package com.lifelab.feature.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.snackbar.Snackbar
import com.lifelab.LifeLabApplication
import com.lifelab.R
import com.lifelab.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding: FragmentRegisterBinding
        get() = checkNotNull(_binding)
    private val viewModel: RegisterViewModel by viewModels {
        val application =
            requireActivity().application as LifeLabApplication

        RegisterViewModel.providerFactory(
            authRepository = application.authRepository,
            dataSyncRepository = application.dataSyncRepository,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerBackButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.openLogin.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.registerButton.setOnClickListener {
            validateAndRegister()
        }

        observeRegisterResult()
    }

    private fun observeRegisterResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
               launch {
                   viewModel.event.collect { event ->
                       when (event) {
                           is RegisterEvent.NavigateToToday -> {
                               findNavController().navigate(
                                   R.id.today_nav_graph,
                                   null,
                                   navOptions {
                                       popUpTo(R.id.auth_nav_graph) {
                                           inclusive = true
                                       }
                                       launchSingleTop = true
                                   }
                               )
                           }

                           is RegisterEvent.ShowMessage -> {
                               Snackbar.make(
                                   binding.root,
                                   event.message,
                                   Snackbar.LENGTH_SHORT,
                               ).show()
                           }
                       }
                   }
               }
               launch {
                   viewModel.isLoading.collect { isLoading ->
                       binding.registerAccountInput.isEnabled =
                           !isLoading

                       binding.registerPasswordInput.isEnabled =
                           !isLoading

                       binding.confirmPasswordInput.isEnabled =
                           !isLoading

                       binding.registerButton.isEnabled =
                           !isLoading

                       binding.registerButton.text = getString(
                           if (isLoading) {
                               R.string.registering
                           } else {
                               R.string.register_action
                           }
                       )
                   }
               }
            }
        }
    }

    private fun validateAndRegister(): Unit = with(binding) {
        val account =
            registerAccountInput.text?.toString()?.trim().orEmpty()

        val password =
            registerPasswordInput.text?.toString().orEmpty()

        val confirmPassword =
            confirmPasswordInput.text?.toString().orEmpty()

        val accountError = when {
            account.isBlank() -> "请输入账号"

            !Regex("^[A-Za-z0-9_]{4,24}$").matches(account) ->
                "账号应为 4 到 24 位字母、数字或下划线"

            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "请输入密码"

            password.length !in 8..128 ->
                "密码长度应为 8 到 128 个字符"

            else -> null
        }
        val confirmPasswordError = when {
            confirmPassword.isBlank() ->
                "请再次输入密码"

            confirmPassword != password ->
                "两次输入的密码不一致"

            else -> null
        }
        registerAccountInputLayout.error = accountError
        registerPasswordInputLayout.error = passwordError
        confirmPasswordInputLayout.error = confirmPasswordError
        if (
            accountError != null ||
            passwordError != null ||
            confirmPasswordError != null
        ) {
            return
        }

        viewModel.register(
            account = account,
            password = password,
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
