package com.lifelab.feature.experiment.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experiments")
data class ExperimentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hypothesis: String,
    val description: String,
    val coverColor: String,
    val startDateMillis: Long,
    val durationDays: Int,
    val baselineDays: Int,
    val interventionDays: Int,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
