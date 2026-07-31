package com.lifelab.feature.experiment.editor

data class ExperimentDraft (
    val name: String = "",
    val hypothesis: String = "",
    val description: String = "",
    val coverColor: String = "purple",

    val startDate: String = "",
    val durationDays: Int = 14,
    val baselineDays: Int = 7,
    val interventionDays: Int = 7,

    val metrics: List<MetricDraft> = emptyList()
)