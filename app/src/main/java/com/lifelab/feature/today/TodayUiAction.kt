package com.lifelab.feature.today

sealed interface TodayUiAction {

    data class SaveRecordClicked(
        val experimentId: Long,
        val values: Map<Long, String>,
        val note: String?,
    ) : TodayUiAction
}
