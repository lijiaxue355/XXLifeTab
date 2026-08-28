package com.lifelab.core.sync.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.lifelab.core.sync.data.local.entity.OutboxEntity

@Dao
interface OutboxDao {

    @Query(
        """
        SELECT * FROM outbox
        ORDER BY createdAtMillis ASC
        LIMIT :limit
        """,
    )
    suspend fun getPending(
        limit: Int,
    ): List<OutboxEntity>

    @Query(
        """
        DELETE FROM outbox
        WHERE id = :outboxId
        """,
    )
    suspend fun deleteById(
        outboxId: Long,
    )
}
