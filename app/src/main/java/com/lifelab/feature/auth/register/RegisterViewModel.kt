package com.lifelab.feature.auth.register

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
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val dataSyncRepository: DataSyncRepository,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _event = MutableSharedFlow<RegisterEvent>()
    val event = _event.asSharedFlow()

    fun register(
        account: String,
        password: String
    ) {
        if (_isLoading.value) {
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = authRepository.register(
                account = account,
                password = password
            )) {
                is AuthResult.Success -> {
                    try {
                        dataSyncRepository.refreshFromServer(
                            result.authResult.id,
                        )
                        _event.emit(RegisterEvent.NavigateToToday)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        _event.emit(
                            RegisterEvent.ShowMessage(
                                error.message
                                    ?: "注册成功，但初始化数据失败",
                            ),
                        )
                    }
                }

                is AuthResult.Failure -> {
                    _event.emit(RegisterEvent.ShowMessage(result.message))
                }
            }
            _isLoading.value = false
        }


    }


    companion object {
        fun providerFactory(
            authRepository: AuthRepository,
            dataSyncRepository: DataSyncRepository,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    RegisterViewModel(
                        authRepository = authRepository,
                        dataSyncRepository = dataSyncRepository,
                    )
                }
            }
        }
    }
}
