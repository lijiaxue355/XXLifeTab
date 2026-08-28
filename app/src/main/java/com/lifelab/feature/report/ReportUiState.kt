package com.lifelab.feature.report

data class ReportPoint(
    val label: String,
    val value: Float,
)

data class ReportUiState(
    val isLoading: Boolean = true,
    val experimentName: String = "",
    val metricName: String = "",
    val recordedDays: Int = 0,
    val totalDays: Int = 0,
    val completionPercent: Int = 0,
    val linePoints: List<ReportPoint> = emptyList(),
    val baselineAverage: Float? = null,
    val interventionAverage: Float? = null,
    val message: String? = null,
)
