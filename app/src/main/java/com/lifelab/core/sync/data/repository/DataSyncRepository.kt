package com.lifelab.core.sync.data.repository

import androidx.room.withTransaction
import com.google.gson.Gson
import com.lifelab.core.database.LifeLabDatabase
import com.lifelab.core.session.AuthTokenStore
import com.lifelab.core.sync.data.remote.RemoteExperimentDto
import com.lifelab.core.sync.data.remote.RemoteRecordDto
import com.lifelab.core.sync.data.remote.SyncApi
import com.lifelab.feature.experiment.data.local.entity.ExperimentEntity
import com.lifelab.feature.experiment.data.local.entity.MetricEntity
import com.lifelab.feature.record.data.local.entity.DailyRecordEntity
import com.lifelab.feature.record.data.local.entity.RecordSyncStatus
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataSyncRepository(
    private val syncApi: SyncApi,
    private val database: LifeLabDatabase,
    private val authTokenStore: AuthTokenStore,
    private val gson: Gson = Gson(),
) {

    suspend fun refreshFromServer(userId: String) {
        val experimentsResponse = syncApi.getExperiments()
        if (!experimentsResponse.isSuccessful) {
            throw IOException(
                "恢复实验失败（${experimentsResponse.code()}）",
            )
        }

        val recordsResponse = syncApi.getRecords()
        if (!recordsResponse.isSuccessful) {
            throw IOException(
                "恢复打卡记录失败（${recordsResponse.code()}）",
            )
        }

        val remoteExperiments =
            experimentsResponse.body().orEmpty()
        val remoteRecords = recordsResponse.body().orEmpty()

        val localDataOwnerId =
            authTokenStore.getLocalDataOwnerId()

        if (
            localDataOwnerId != null &&
            localDataOwnerId != userId
        ) {
            withContext(Dispatchers.IO) {
                database.clearAllTables()
            }
        }

        database.withTransaction {
            saveExperiments(remoteExperiments)
            saveRecords(remoteRecords)
        }

        authTokenStore.saveLocalDataOwnerId(userId)
    }

    private suspend fun saveExperiments(
        remoteExperiments: List<RemoteExperimentDto>,
    ) {
        val experimentDao = database.experimentDao()

        remoteExperiments.forEach { remote ->
            experimentDao.saveRemoteExperiment(
                experiment = ExperimentEntity(
                    syncId = remote.id,
                    name = remote.name,
                    hypothesis = remote.hypothesis,
                    description = remote.description,
                    coverColor = remote.coverColor,
                    startDateMillis = remote.startDateMillis,
                    durationDays = remote.durationDays,
                    baselineDays = remote.baselineDays,
                    interventionDays = remote.interventionDays,
                    createdAtMillis = remote.createdAtMillis,
                ),
                metrics = remote.metrics.map { metric ->
                    MetricEntity(
                        syncId = metric.id,
                        experimentId = 0,
                        name = metric.name,
                        type = metric.type,
                        required = metric.required,
                        sortOrder = metric.sortOrder,
                    )
                },
            )
        }
    }

    private suspend fun saveRecords(
        remoteRecords: List<RemoteRecordDto>,
    ) {
        val experimentDao = database.experimentDao()
        val recordDao = database.recordDao()

        remoteRecords.forEach { remote ->
            val experimentId =
                experimentDao.getExperimentIdBySyncId(
                    remote.experimentId,
                ) ?: return@forEach

            val experiment =
                experimentDao.getExperiment(experimentId)
                    ?: return@forEach

            val metricIdsBySyncId =
                experiment.metrics.associate { metric ->
                    metric.syncId to metric.id
                }

            val localValues = remote.values.mapNotNull { value ->
                val localMetricId =
                    metricIdsBySyncId[value.metricId]
                        ?: return@mapNotNull null

                localMetricId.toString() to value.value
            }.toMap()

            val recordDate = SimpleDateFormat(
                DATE_PATTERN,
                Locale.US,
            ).format(Date(remote.recordedAtMillis))

            val sameDayRecord = recordDao.getRecord(
                experimentId = experimentId,
                recordDate = recordDate,
            )

            if (sameDayRecord?.syncStatus == RecordSyncStatus.PENDING) {
                return@forEach
            }

            val localRecordId = sameDayRecord?.id
                ?: recordDao.getRecordIdBySyncId(remote.id)
                ?: 0L

            recordDao.insertOrReplaceRecord(
                DailyRecordEntity(
                    id = localRecordId,
                    syncId = remote.id,
                    experimentId = experimentId,
                    recordDate = recordDate,
                    valuesJson = gson.toJson(localValues),
                    note = remote.note,
                    syncStatus = RecordSyncStatus.SYNCED,
                    recordedAtMillis = remote.recordedAtMillis,
                ),
            )
        }
    }

    companion object {
        private const val DATE_PATTERN = "yyyy-MM-dd"
    }
}
