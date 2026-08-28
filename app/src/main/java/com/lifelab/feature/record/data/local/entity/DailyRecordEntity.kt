package com.lifelab.feature.record.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import java.util.UUID

@Entity(
    tableName = "daily_records",
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
        Index(
            value = ["syncId"],
            unique = true,
        ),
        Index(
            value = ["experimentId", "recordDate"],
            unique = true,
        ),
    ],
)
data class DailyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncId: String = UUID.randomUUID().toString(),
    val experimentId: Long,
    val recordDate: String,
    val valuesJson: String,
    val note: String? = null,
    val syncStatus: String = RecordSyncStatus.PENDING,
    val recordedAtMillis: Long = System.currentTimeMillis(),
)

object RecordSyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
}
