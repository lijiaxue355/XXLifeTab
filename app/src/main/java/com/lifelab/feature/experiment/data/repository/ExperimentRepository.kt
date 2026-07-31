package com.lifelab.feature.experiment.data.repository

import com.lifelab.feature.experiment.data.local.dao.ExperimentDao
import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import com.lifelab.feature.experiment.data.local.entity.MetricEntity
import com.lifelab.feature.experiment.data.local.relation.ExperimentWithMetrics
import kotlinx.coroutines.flow.Flow

class ExperimentRepository(private val experimentDao: ExperimentDao) {

    val experiments: Flow<List<ExperimentWithMetrics>> = experimentDao.observeExperiments()

    suspend fun createExperiment(
        experiment: ExperimentEntity,
        metrics: List<MetricEntity>
    ): Long {
        return experimentDao.insertExperimentWithMetrics(
            experiment,metrics
        )

    }
    suspend fun getExperiment(
        experimentId: Long,
    ): ExperimentWithMetrics? {
        return experimentDao.getExperiment(experimentId)
    }
}