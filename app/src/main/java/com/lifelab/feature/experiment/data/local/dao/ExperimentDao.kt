package com.lifelab.feature.experiment.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
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
}
