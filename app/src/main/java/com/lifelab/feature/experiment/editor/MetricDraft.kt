package com.lifelab.feature.experiment.editor

enum class MetricType {
    YES_NO,
    DURATION,
    DECIMAL,
    SCORE,
}

data class MetricDraft(
    val name: String = "",
    val type: MetricType = MetricType.YES_NO,
    val required: Boolean = true,
)
