package com.lifelab.feature.experiment.data.repository

import android.content.Context
import com.lifelab.core.sync.work.SyncScheduler
import com.lifelab.core.sync.data.remote.SyncApi
import com.lifelab.feature.experiment.data.local.dao.ExperimentDao
import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import com.lifelab.feature.experiment.data.local.entity.MetricEntity
import com.lifelab.feature.experiment.data.local.relation.ExperimentWithMetrics
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class ExperimentRepository(
    private val experimentDao: ExperimentDao,
    private val context: Context,
    private val syncApi: SyncApi,
) {

    val experiments: Flow<List<ExperimentWithMetrics>> = experimentDao.observeExperiments()

    suspend fun createExperiment(
        experiment: ExperimentEntity,
        metrics: List<MetricEntity>
    ): Long {
        val experimentId =
            experimentDao.insertExperimentWithMetrics(
                experiment = experiment,
                metrics = metrics,
            )

        SyncScheduler.enqueue(context)

        return experimentId

    }

    suspend fun getExperiment(
        experimentId: Long,
    ): ExperimentWithMetrics? {
        return experimentDao.getExperiment(experimentId)
    }

    suspend fun deleteExperiment(experimentId: Long) {
        val experiment = experimentDao
            .getExperiment(experimentId)
            ?.experiment
            ?: return

        val response = syncApi.deleteExperiment(
            experiment.syncId,
        )

        if (!response.isSuccessful) {
            throw IOException(
                "服务端删除失败（${response.code()}）",
            )
        }

        experimentDao.deleteExperiment(experimentId)
    }
}
