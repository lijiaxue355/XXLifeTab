package com.lifelab.feature.auth.login

sealed interface  LoginUiAction {
    data class AccountChanged(
        val account: String,
    ) : LoginUiAction

    data class PasswordChanged(
        val password: String,
    ) : LoginUiAction

    data object LoginClicked : LoginUiAction
}