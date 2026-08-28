package com.lifelab.feature.today

sealed interface TodayUiEffect {

    data class ShowMessage(
        val message: String,
    ) : TodayUiEffect
}