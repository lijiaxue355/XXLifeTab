package com.lifelab.feature.today

import com.lifelab.feature.experiment.data.local.relation.ExperimentWithMetrics

data class TodayExperimentItem(
    val experiment: ExperimentWithMetrics,
    val values: Map<Long, String> = emptyMap(),
    val note: String = "",
    val hasRecord: Boolean = false,
    val syncStatus: String? = null,
    val isSaving: Boolean = false,
)

data class TodayUiState(
    val items: List<TodayExperimentItem> = emptyList(),
)
