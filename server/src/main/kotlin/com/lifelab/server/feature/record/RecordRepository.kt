package com.lifelab.server.feature.record

import com.lifelab.server.core.database.DailyRecordsTable
import com.lifelab.server.core.database.DatabaseFactory
import com.lifelab.server.core.database.ExperimentsTable
import com.lifelab.server.core.database.MetricsTable
import com.lifelab.server.core.database.RecordValuesTable
import com.lifelab.server.core.error.ApiException
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class RecordRepository {
    suspend fun getRecords(
        userId: String,
        experimentId: String?,
        fromMillis: Long?,
        toMillis: Long?,
    ): List<RecordResponse> = DatabaseFactory.dbQuery {
        DailyRecordsTable
            .selectAll()
            .where { DailyRecordsTable.userId eq userId }
            .apply {
                if (experimentId != null) andWhere { DailyRecordsTable.experimentId eq experimentId }
                if (fromMillis != null) andWhere { DailyRecordsTable.recordedAtMillis greaterEq fromMillis }
                if (toMillis != null) andWhere { DailyRecordsTable.recordedAtMillis lessEq toMillis }
            }
            .orderBy(DailyRecordsTable.recordedAtMillis, SortOrder.DESC)
            .map(::toRecordResponse)
    }

    suspend fun createOrReplace(userId: String, request: CreateRecordRequest): RecordResponse {
        validate(request)
        val recordId = request.id.toUuidOrNew()
        return DatabaseFactory.dbQuery {
            val experiment = ExperimentsTable
                .selectAll()
                .where { ExperimentsTable.id eq request.experimentId }
                .singleOrNull()
                ?: throw ApiException(HttpStatusCode.NotFound, "EXPERIMENT_NOT_FOUND", "实验不存在")
            if (experiment[ExperimentsTable.userId] != userId) {
                throw ApiException(HttpStatusCode.NotFound, "EXPERIMENT_NOT_FOUND", "实验不存在")
            }

            val metrics = MetricsTable
                .selectAll()
                .where { MetricsTable.experimentId eq request.experimentId }
                .toList()
            val metricIds = metrics.map { it[MetricsTable.id] }.toSet()
            val submittedMetricIds = request.values.map { it.metricId }
            if (submittedMetricIds.distinct().size != submittedMetricIds.size ||
                submittedMetricIds.any { it !in metricIds }
            ) {
                throw ApiException(HttpStatusCode.BadRequest, "INVALID_RECORD_VALUES", "打卡数据包含无效或重复指标")
            }
            val missingRequired = metrics
                .filter { it[MetricsTable.required] }
                .map { it[MetricsTable.id] }
                .filterNot(submittedMetricIds::contains)
            if (missingRequired.isNotEmpty()) {
                throw ApiException(HttpStatusCode.BadRequest, "MISSING_REQUIRED_METRIC", "请填写全部必填指标")
            }

            val existing = DailyRecordsTable
                .selectAll()
                .where { DailyRecordsTable.id eq recordId }
                .singleOrNull()
            if (existing != null && existing[DailyRecordsTable.userId] != userId) {
                throw ApiException(HttpStatusCode.Conflict, "RECORD_ID_CONFLICT", "打卡记录 ID 已被占用")
            }

            val now = System.currentTimeMillis()
            if (existing == null) {
                DailyRecordsTable.insert {
                    it[id] = recordId
                    it[DailyRecordsTable.userId] = userId
                    it[experimentId] = request.experimentId
                    it[recordedAtMillis] = request.recordedAtMillis
                    it[note] = request.note?.trim()
                    it[createdAtMillis] = now
                    it[updatedAtMillis] = now
                }
            } else {
                DailyRecordsTable.update({ DailyRecordsTable.id eq recordId }) {
                    it[experimentId] = request.experimentId
                    it[recordedAtMillis] = request.recordedAtMillis
                    it[note] = request.note?.trim()
                    it[updatedAtMillis] = now
                }
                RecordValuesTable.deleteWhere { RecordValuesTable.recordId eq recordId }
            }

            request.values.forEach { recordValue ->
                RecordValuesTable.insert {
                    it[id] = UUID.randomUUID().toString()
                    it[RecordValuesTable.recordId] = recordId
                    it[metricId] = recordValue.metricId
                    it[value] = recordValue.value.trim()
                }
            }
            toRecordResponse(
                DailyRecordsTable.selectAll().where { DailyRecordsTable.id eq recordId }.single(),
            )
        }
    }

    private fun toRecordResponse(row: ResultRow): RecordResponse {
        val recordId = row[DailyRecordsTable.id]
        return RecordResponse(
            id = recordId,
            experimentId = row[DailyRecordsTable.experimentId],
            recordedAtMillis = row[DailyRecordsTable.recordedAtMillis],
            note = row[DailyRecordsTable.note],
            createdAtMillis = row[DailyRecordsTable.createdAtMillis],
            updatedAtMillis = row[DailyRecordsTable.updatedAtMillis],
            values = RecordValuesTable
                .selectAll()
                .where { RecordValuesTable.recordId eq recordId }
                .map {
                    RecordValueResponse(
                        metricId = it[RecordValuesTable.metricId],
                        value = it[RecordValuesTable.value],
                    )
                },
        )
    }

    private fun validate(request: CreateRecordRequest) {
        request.experimentId.requireUuid("实验 ID")
        if (request.recordedAtMillis <= 0) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_RECORD_TIME", "打卡时间无效")
        }
        if ((request.note?.length ?: 0) > 2_000 || request.values.isEmpty() || request.values.size > 20) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_RECORD", "打卡内容无效")
        }
        request.values.forEach {
            it.metricId.requireUuid("指标 ID")
            if (it.value.isBlank() || it.value.length > 500) {
                throw ApiException(HttpStatusCode.BadRequest, "INVALID_RECORD_VALUE", "指标值不能为空或过长")
            }
        }
    }

    private fun String?.toUuidOrNew(): String {
        if (this == null) return UUID.randomUUID().toString()
        requireUuid("记录 ID")
        return UUID.fromString(this).toString()
    }

    private fun String.requireUuid(fieldName: String) {
        if (runCatching(UUID::fromString).isFailure) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_ID", "$fieldName 格式无效")
        }
    }
}
