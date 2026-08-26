package com.lifelab.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lifelab.feature.auth.data.repository.AuthRepository
import com.lifelab.feature.auth.domain.model.AuthResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

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
                    _event.emit(RegisterEvent.NavigateToToday)
                }

                is AuthResult.Failure -> {
                    _event.emit(RegisterEvent.ShowMessage(result.message))
                }
            }
            _isLoading.value = false
        }


    }


    companion object {
        fun providerFactory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    RegisterViewModel(authRepository)
                }
            }
        }
    }
}