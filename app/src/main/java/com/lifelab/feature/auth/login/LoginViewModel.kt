package com.lifelab.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lifelab.feature.auth.data.repository.AuthRepository
import com.lifelab.feature.auth.domain.model.AuthResult
import com.lifelab.core.sync.data.repository.DataSyncRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val dataSyncRepository: DataSyncRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEvent = MutableSharedFlow<LoginUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun dispatch(action: LoginUiAction) {
        when (action) {
            is LoginUiAction.AccountChanged -> {
                _uiState.update {
                    it.copy(
                        account = action.account,
                        accountError = null
                    )
                }
            }

            is LoginUiAction.PasswordChanged -> {
                _uiState.update {
                    it.copy(
                        password = action.password,
                        passwordError = null,
                    )
                }
            }

            LoginUiAction.LoginClicked -> {
                login()
            }
        }

    }

    private fun login() {
        val state = _uiState.value
        val accountError = if (state.account.isBlank()) {
            "请输入账号"
        } else {
            null
        }
        val passwordError = if (state.password.isBlank()) {
            "请输入密码"
        } else {
            null
        }

        if (accountError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    accountError = accountError,
                    passwordError = passwordError,
                )
            }
            return
        }

        if (state.isLoading) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = authRepository.login(
                account = state.account,
                password = state.password
            )){
                is AuthResult.Success -> {
                    try {
                        dataSyncRepository.refreshFromServer(
                            result.authResult.id,
                        )
                        _uiEvent.emit(
                            LoginUiEvent.NavigateToToday,
                        )
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        _uiEvent.emit(
                            LoginUiEvent.ShowMessage(
                                error.message
                                    ?: "登录成功，但恢复数据失败",
                            ),
                        )
                    } finally {
                        _uiState.update {
                            it.copy(isLoading = false)
                        }
                    }
                }
                is AuthResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }

                    _uiEvent.emit(
                        LoginUiEvent.ShowMessage(result.message)
                    )
                }
            }
        }


    }


    companion object {
        fun provideFactory(
            authRepository: AuthRepository,
            dataSyncRepository: DataSyncRepository,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    LoginViewModel(
                        authRepository = authRepository,
                        dataSyncRepository = dataSyncRepository,
                    )
                }
            }
        }
    }
}
