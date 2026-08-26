package com.lifelab.feature.auth.register

sealed interface RegisterEvent {
    data object NavigateToToday : RegisterEvent

    data class ShowMessage(
        val message: String,
    ) : RegisterEvent
}
