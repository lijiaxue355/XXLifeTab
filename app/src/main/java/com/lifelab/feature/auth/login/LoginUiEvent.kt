package com.lifelab.feature.auth.login

sealed interface LoginUiEvent {

    data object NavigateToToday : LoginUiEvent

    data class ShowMessage(
        val message: String,
    ) : LoginUiEvent
}