package com.lifelab.feature.record.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lifelab.core.sync.work.SyncScheduler
import com.lifelab.feature.record.data.local.dao.RecordDao
import com.lifelab.feature.record.data.local.entity.DailyRecordEntity
import com.lifelab.feature.record.data.local.entity.RecordSyncStatus
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RecordRepository(
    private val recordDao: RecordDao,
    private val context: Context,
    private val gson: Gson = Gson()
) {
    private val valuesType = object : TypeToken<Map<String, String>>() {}.type

    fun observeTodayRecord(experimentId: Long): Flow<DailyRecordEntity?> {
        return recordDao.observeRecord(
            experimentId = experimentId,
            recordDate = today()
        )
    }

    fun observeTodayRecords(): Flow<List<DailyRecordEntity>> {
        return recordDao.observeRecordsByDate(today())
    }

    fun observeAllRecords(): Flow<List<DailyRecordEntity>> {
        return recordDao.observeAllRecords()
    }

    fun observeRecords(
        experimentId: Long
    ): Flow<List<DailyRecordEntity>> {
        return recordDao.observeRecords(
            experimentId = experimentId
        )
    }

    suspend fun saveTodayRecord(
        experimentId: Long,
        values: Map<Long, String>,
        note: String?
    ): Long {
        val recordDate = today()

        val existingRecord = recordDao.getRecord(
            experimentId = experimentId,
            recordDate = recordDate,
        )

        val stringValues = values.mapKeys { entry ->
            entry.key.toString()
        }
        val record = DailyRecordEntity(
            id = existingRecord?.id ?: 0,
            syncId = existingRecord?.syncId
                ?: UUID.randomUUID().toString(),
            experimentId = experimentId,
            recordDate = recordDate,
            valuesJson = gson.toJson(stringValues),
            note = note
                ?.trim()
                ?.takeIf { it.isNotEmpty() },
            syncStatus = RecordSyncStatus.PENDING,
            recordedAtMillis = System.currentTimeMillis(),
        )

        val recordId =
            recordDao.saveRecordWithOutbox(record)

        SyncScheduler.enqueue(context)

        return recordId

    }

    fun decodeValues(
        valuesJson: String,
    ): Map<Long, String> {
        val stringValues =
            gson.fromJson<Map<String, String>>(
                valuesJson,
                valuesType,
            ) ?: emptyMap()

        return stringValues.mapNotNull { entry ->
            val metricId = entry.key.toLongOrNull()

            if (metricId == null) {
                null
            } else {
                metricId to entry.value
            }
        }.toMap()
    }


    private fun today(): String {
        return SimpleDateFormat(
            DATE_PATTERN,
            Locale.US,
        ).format(Date())
    }

    companion object {
        private const val DATE_PATTERN = "yyyy-MM-dd"
    }
}
