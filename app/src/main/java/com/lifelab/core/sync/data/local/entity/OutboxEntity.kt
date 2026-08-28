package com.lifelab.core.sync.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbox",
    indices = [
        Index(
            value = ["aggregateType", "aggregateLocalId"],
            unique = true,
        ),
    ],
)
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val aggregateType: String,
    val aggregateLocalId: Long,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

object OutboxAggregateType {
    const val EXPERIMENT = "EXPERIMENT"
    const val DAILY_RECORD = "DAILY_RECORD"
}
