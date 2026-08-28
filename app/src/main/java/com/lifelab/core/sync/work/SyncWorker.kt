package com.lifelab.core.sync.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lifelab.LifeLabApplication
import com.lifelab.core.network.NetworkModule
import com.lifelab.core.sync.data.local.entity.OutboxAggregateType
import com.lifelab.core.sync.data.local.entity.OutboxEntity
import com.lifelab.core.sync.data.remote.UploadExperimentRequestDto
import com.lifelab.core.sync.data.remote.UploadMetricRequestDto
import com.lifelab.core.sync.data.remote.UploadRecordRequestDto
import com.lifelab.core.sync.data.remote.UploadRecordValueRequestDto
import com.lifelab.feature.record.data.local.entity.RecordSyncStatus
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class SyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val application = applicationContext as LifeLabApplication
        val outboxDao = application.database.outboxDao()

        return try {
            while (true) {
                val pendingItems = outboxDao.getPending(limit = BATCH_SIZE)
                if (pendingItems.isEmpty()) {
                    break
                }
                for (outbox in pendingItems) {
                    val successful = syncOutboxItem(
                        application = application,
                        outbox = outbox,
                    )
                    if (!successful) {
                        return Result.retry()
                    }
                    outboxDao.deleteById(outbox.id)
                }

            }
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            Result.retry()
        } catch (error: Exception) {
            Result.retry()
        }
    }

    private suspend fun syncOutboxItem(
        application: LifeLabApplication,
        outbox: OutboxEntity
    ): Boolean {
        return when (outbox.aggregateType) {
            OutboxAggregateType.EXPERIMENT -> {
                syncExperiment(
                    application = application,
                    localExperimentId = outbox.aggregateLocalId,
                )
            }

            OutboxAggregateType.DAILY_RECORD -> {
                syncRecord(
                    application = application,
                    localRecordId = outbox.aggregateLocalId,
                )
            }

            else -> true

        }
    }

    private suspend fun syncRecord(
        application: LifeLabApplication,
        localRecordId: Long
    ): Boolean {
        val recordDao = application.database.recordDao()

        val record =
            recordDao.getRecordById(localRecordId)
                ?: return true
        val experimentWithMetrics =
            application.database
                .experimentDao()
                .getExperiment(record.experimentId)
                ?: return true
        val values =
            application.recordRepository.decodeValues(
                record.valuesJson,
            )
        val metricsByLocalId =
            experimentWithMetrics.metrics.associateBy { metric ->
                metric.id
            }
        val requestValues =
            mutableListOf<UploadRecordValueRequestDto>()

        for ((localMetricId, value) in values) {
            val metric = metricsByLocalId[localMetricId]
                ?: return false

            requestValues += UploadRecordValueRequestDto(
                metricId = metric.syncId,
                value = value,
            )
        }
        val request = UploadRecordRequestDto(
            id = record.syncId,
            experimentId =
                experimentWithMetrics.experiment.syncId,
            recordedAtMillis = record.recordedAtMillis,
            note = record.note,
            values = requestValues,
        )
        val response =
            NetworkModule.syncApi.uploadRecord(request)

        if (!response.isSuccessful) {
            return false
        }

        recordDao.updateSyncStatus(
            recordId = record.id,
            syncStatus = RecordSyncStatus.SYNCED,
        )

        return true


    }

    private suspend  fun syncExperiment(
        application: LifeLabApplication,
        localExperimentId: Long
    ): Boolean {
        val experimentWithMetrics = application.database.experimentDao()
            .getExperiment(localExperimentId) ?: return true
        val experiment = experimentWithMetrics.experiment
        val request =  UploadExperimentRequestDto(
            id = experiment.syncId,
            name = experiment.name,
            hypothesis = experiment.hypothesis,
            description = experiment.description,
            coverColor = experiment.coverColor,
            startDateMillis = experiment.startDateMillis,
            durationDays = experiment.durationDays,
            baselineDays = experiment.baselineDays,
            interventionDays = experiment.interventionDays,
            metrics = experimentWithMetrics.metrics.map { metric ->
                UploadMetricRequestDto(
                    id = metric.syncId,
                    name = metric.name,
                    type = metric.type,
                    required = metric.required,
                    sortOrder = metric.sortOrder,
                )
            },
        )
        val response =
            NetworkModule.syncApi.uploadExperiment(request)

        return response.isSuccessful

    }


    companion object {
        private const val BATCH_SIZE = 50
    }
}