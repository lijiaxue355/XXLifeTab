package com.lifelab.feature.record.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lifelab.core.sync.data.local.entity.OutboxAggregateType
import com.lifelab.core.sync.data.local.entity.OutboxEntity
import com.lifelab.feature.record.data.local.entity.DailyRecordEntity
import com.lifelab.feature.record.data.local.entity.RecordSyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query(
        """
        SELECT * FROM daily_records
        WHERE experimentId = :experimentId
          AND recordDate = :recordDate
        LIMIT 1
        """,
    )
    fun observeRecord(
        experimentId: Long,
        recordDate: String,
    ): Flow<DailyRecordEntity?>

    @Query(
        """
        SELECT * FROM daily_records
        WHERE recordDate = :recordDate
        ORDER BY recordedAtMillis DESC
        """,
    )
    fun observeRecordsByDate(
        recordDate: String,
    ): Flow<List<DailyRecordEntity>>

    @Query(
        """
        SELECT * FROM daily_records
        ORDER BY recordedAtMillis ASC
        """,
    )
    fun observeAllRecords(): Flow<List<DailyRecordEntity>>

    @Query(
        """
        SELECT * FROM daily_records
        WHERE experimentId = :experimentId
          AND recordDate = :recordDate
        LIMIT 1
        """,
    )
    suspend fun getRecord(
        experimentId: Long,
        recordDate: String,
    ): DailyRecordEntity?

    @Query(
        """
        SELECT * FROM daily_records
        WHERE experimentId = :experimentId
        ORDER BY recordDate ASC
        """,
    )
    fun observeRecords(
        experimentId: Long,
    ): Flow<List<DailyRecordEntity>>

    @Query(
        """
        SELECT * FROM daily_records
        WHERE id = :recordId
        LIMIT 1
        """,
    )
    suspend fun getRecordById(
        recordId: Long,
    ): DailyRecordEntity?

    @Query(
        """
        SELECT id FROM daily_records
        WHERE syncId = :syncId
        LIMIT 1
        """,
    )
    suspend fun getRecordIdBySyncId(syncId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceRecord(
        record: DailyRecordEntity,
    ): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceOutbox(
        outbox: OutboxEntity,
    )

    @Query(
        """
        UPDATE daily_records
        SET syncStatus = :syncStatus
        WHERE id = :recordId
        """,
    )
    suspend fun updateSyncStatus(
        recordId: Long,
        syncStatus: String,
    )

    @Transaction
    suspend fun saveRecordWithOutbox(
        record: DailyRecordEntity,
    ): Long {
        val recordId = insertOrReplaceRecord(
            record.copy(syncStatus = RecordSyncStatus.PENDING),
        )

        insertOrReplaceOutbox(
            OutboxEntity(
                aggregateType = OutboxAggregateType.DAILY_RECORD,
                aggregateLocalId = recordId,
            ),
        )

        return recordId
    }
}
