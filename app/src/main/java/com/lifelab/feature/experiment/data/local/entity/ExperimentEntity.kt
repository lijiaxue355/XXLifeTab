package com.lifelab.feature.experiment.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "experiments",
    indices = [
        Index(
            value = ["syncId"],
            unique = true,
        ),
    ],
)
data class ExperimentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
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
