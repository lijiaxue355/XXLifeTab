package com.lifelab.feature.experiment.editor

data class ExperimentDraft(
    val name: String = "",
    val hypothesis: String = "",
    val description: String = "",
    val coverColor: String = "purple",

    val startDateMillis: Long = System.currentTimeMillis(),
    val durationDays: Int = 14,
    val baselineDays: Int = 7,
    val interventionDays: Int = 7,

    val metrics: List<MetricDraft> = listOf(
        MetricDraft(
            type = MetricType.DECIMAL,
            required = true,
        ),
    ),
)
