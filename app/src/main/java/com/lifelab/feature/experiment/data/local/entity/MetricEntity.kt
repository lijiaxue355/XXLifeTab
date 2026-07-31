package com.lifelab.feature.experiment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "metrics",
    foreignKeys = [
        ForeignKey(
            entity = ExperimentEntity::class,
            parentColumns = ["id"],
            childColumns = ["experimentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["experimentId"]),
    ],
)
data class MetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val experimentId: Long,
    val name: String,
    val type: String,
    val required: Boolean,
    val sortOrder: Int,
)
