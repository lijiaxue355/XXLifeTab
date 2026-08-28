package com.lifelab.feature.experiment.data.mapper

import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import com.lifelab.feature.experiment.data.local.entity.MetricEntity
import com.lifelab.feature.experiment.editor.ExperimentDraft

fun ExperimentDraft.toExperimentEntity(): ExperimentEntity{
    return ExperimentEntity(
        name = name,
        hypothesis = hypothesis,
        description = description,
        coverColor = coverColor,
        startDateMillis = startDateMillis,
        durationDays = durationDays,
        baselineDays = baselineDays,
        interventionDays = interventionDays
    )
}

fun ExperimentDraft.toMetricEntities(): List<MetricEntity> {
    return metrics.mapIndexed { index, metric ->
        MetricEntity(
            experimentId = 0,
            name = metric.name.trim(),
            type = metric.type.name,
            required = metric.required,
            sortOrder = index
        )
    }
}
