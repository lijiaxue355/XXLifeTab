package com.lifelab.feature.experiment.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lifelab.core.sync.data.local.entity.OutboxAggregateType
import com.lifelab.core.sync.data.local.entity.OutboxEntity
import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import com.lifelab.feature.experiment.data.local.entity.MetricEntity
import com.lifelab.feature.experiment.data.local.relation.ExperimentWithMetrics
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperimentDao {

    @Insert
    suspend fun insertExperiment(experiment: ExperimentEntity): Long

    @Insert
    suspend fun insertMetrics(metrics: List<MetricEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceExperiment(experiment: ExperimentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceMetrics(metrics: List<MetricEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceOutbox(outbox: OutboxEntity)

    @Transaction
    suspend fun insertExperimentWithMetrics(
        experiment: ExperimentEntity,
        metrics: List<MetricEntity>,
    ): Long {
        val experimentId = insertExperiment(experiment)

        val metricsWithExperimentId = metrics.map { metric ->
            metric.copy(experimentId = experimentId)
        }

        insertMetrics(metricsWithExperimentId)

        insertOrReplaceOutbox(
            OutboxEntity(
                aggregateType = OutboxAggregateType.EXPERIMENT,
                aggregateLocalId = experimentId,
            ),
        )

        return experimentId
    }

    @Transaction
    @Query(
        """
        SELECT * FROM experiments
        ORDER BY createdAtMillis DESC
        """,
    )
    fun observeExperiments(): Flow<List<ExperimentWithMetrics>>

    @Transaction
    @Query(
        """
        SELECT * FROM experiments
        WHERE id = :experimentId
        """,
    )
    suspend fun getExperiment(experimentId: Long): ExperimentWithMetrics?

    @Query("SELECT id FROM experiments WHERE syncId = :syncId LIMIT 1")
    suspend fun getExperimentIdBySyncId(syncId: String): Long?

    @Query("SELECT id FROM metrics WHERE syncId = :syncId LIMIT 1")
    suspend fun getMetricIdBySyncId(syncId: String): Long?

    @Query("DELETE FROM experiments WHERE id = :experimentId")
    suspend fun deleteExperiment(experimentId: Long)

    @Transaction
    suspend fun saveRemoteExperiment(
        experiment: ExperimentEntity,
        metrics: List<MetricEntity>,
    ): Long {
        val localExperimentId = getExperimentIdBySyncId(
            experiment.syncId,
        ) ?: 0L

        val savedExperimentId = insertOrReplaceExperiment(
            experiment.copy(id = localExperimentId),
        )

        val localMetrics = metrics.map { metric ->
            metric.copy(
                id = getMetricIdBySyncId(metric.syncId) ?: 0L,
                experimentId = savedExperimentId,
            )
        }

        insertOrReplaceMetrics(localMetrics)
        return savedExperimentId
    }
}
