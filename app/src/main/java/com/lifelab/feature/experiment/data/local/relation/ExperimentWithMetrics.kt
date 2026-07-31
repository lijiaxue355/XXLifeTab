package com.lifelab.feature.experiment.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import com.lifelab.feature.experiment.data.local.entity.MetricEntity

data class ExperimentWithMetrics(
    @Embedded
    val experiment: ExperimentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "experimentId",
    )
    val metrics: List<MetricEntity>,
)
