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
            name = "是否完成干预",
            type = MetricType.YES_NO,
            required = true,
        ),
        MetricDraft(
            name = "入睡耗时",
            type = MetricType.DURATION,
            required = true,
        ),
        MetricDraft(
            name = "睡眠时长",
            type = MetricType.DECIMAL,
            required = true,
        ),
        MetricDraft(
            name = "晨间精力",
            type = MetricType.SCORE,
            required = true,
        ),
    ),
)
