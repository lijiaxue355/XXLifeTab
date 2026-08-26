package com.lifelab.feature.auth.login

data class LoginUiState (
    val account: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val accountError: String? = null,
    val passwordError: String? = null
)