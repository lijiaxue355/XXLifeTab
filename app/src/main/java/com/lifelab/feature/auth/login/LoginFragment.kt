package com.lifelab.feature.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
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
import com.lifelab.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding
        get() = checkNotNull(_binding)
    private val viewModel: LoginViewModel by viewModels {
        val application = requireActivity().application as LifeLabApplication
        LoginViewModel.provideFactory(
            application.authRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindActions()
        observeUi()
    }

    private fun bindActions() = with(binding) {
        accountInput.doAfterTextChanged {
            viewModel.dispatch(
                LoginUiAction.AccountChanged(
                    it?.toString().orEmpty()
                )
            )
        }
        passwordInput.doAfterTextChanged {
            viewModel.dispatch(
                LoginUiAction.PasswordChanged(
                    it?.toString().orEmpty()
                )
            )
        }
        loginButton.setOnClickListener {
            viewModel.dispatch(LoginUiAction.LoginClicked)
        }
        openRegister.setOnClickListener {
            findNavController().navigate(
                R.id.action_loginFragment_to_registerFragment
            )
        }
    }

    private fun observeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is LoginUiEvent.NavigateToToday -> {
                            findNavController().navigate(
                                R.id.today_nav_graph,
                                null, navOptions {
                                    popUpTo(R.id.auth_nav_graph) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                })
                        }

                        is LoginUiEvent.ShowMessage -> {
                            Snackbar.make(
                                binding.root,
                                event.message,
                                Snackbar.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun render(state: LoginUiState) = with(binding) {
        if (accountInput.text?.toString() != state.account) {
            accountInput.setText(state.account)
        }

        if (passwordInput.text?.toString() != state.password) {
            passwordInput.setText(state.password)
        }

        accountInputLayout.error = state.accountError
        passwordInputLayout.error = state.passwordError

        accountInput.isEnabled = !state.isLoading
        passwordInput.isEnabled = !state.isLoading
        openRegister.isEnabled = !state.isLoading
        loginButton.isEnabled = !state.isLoading

        loginButton.text = getString(
            if (state.isLoading) {
                R.string.logging_in
            } else {
                R.string.login_action
            }
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
