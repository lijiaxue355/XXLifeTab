package com.lifelab.server.feature.experiment

import com.lifelab.server.core.database.DatabaseFactory
import com.lifelab.server.core.database.ExperimentsTable
import com.lifelab.server.core.database.MetricsTable
import com.lifelab.server.core.database.TemplatesTable
import com.lifelab.server.core.error.ApiException
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class ExperimentRepository {
    suspend fun getExperiments(userId: String): List<ExperimentResponse> = DatabaseFactory.dbQuery {
        ExperimentsTable
            .selectAll()
            .where { ExperimentsTable.userId eq userId }
            .orderBy(ExperimentsTable.createdAtMillis, SortOrder.DESC)
            .map(::toExperimentResponse)
    }

    suspend fun createOrReplace(userId: String, request: CreateExperimentRequest): ExperimentResponse {
        validate(request)
        val experimentId = request.id.toUuidOrNew("实验 ID")
        val metricIds = request.metrics.map { it.id.toUuidOrNew("指标 ID") }
        if (metricIds.distinct().size != metricIds.size) {
            throw ApiException(HttpStatusCode.BadRequest, "DUPLICATE_METRIC_ID", "指标 ID 不能重复")
        }

        return DatabaseFactory.dbQuery {
            if (request.templateId != null && TemplatesTable
                    .selectAll()
                    .where { TemplatesTable.id eq request.templateId }
                    .empty()
            ) {
                throw ApiException(HttpStatusCode.BadRequest, "TEMPLATE_NOT_FOUND", "实验模板不存在")
            }

            val existing = ExperimentsTable
                .selectAll()
                .where { ExperimentsTable.id eq experimentId }
                .singleOrNull()
            if (existing != null && existing[ExperimentsTable.userId] != userId) {
                throw ApiException(HttpStatusCode.Conflict, "EXPERIMENT_ID_CONFLICT", "实验 ID 已被占用")
            }

            val now = System.currentTimeMillis()
            if (existing == null) {
                ExperimentsTable.insert {
                    it[id] = experimentId
                    it[ExperimentsTable.userId] = userId
                    writeExperimentFields(it, request, now, now)
                }
            } else {
                ExperimentsTable.update({ ExperimentsTable.id eq experimentId }) {
                    writeExperimentFields(it, request, existing[ExperimentsTable.createdAtMillis], now)
                }
                MetricsTable.deleteWhere { MetricsTable.experimentId eq experimentId }
            }

            request.metrics.forEachIndexed { index, metric ->
                MetricsTable.insert {
                    it[id] = metricIds[index]
                    it[MetricsTable.experimentId] = experimentId
                    it[name] = metric.name.trim()
                    it[type] = metric.type.uppercase()
                    it[required] = metric.required
                    it[sortOrder] = metric.sortOrder
                }
            }
            toExperimentResponse(
                ExperimentsTable.selectAll().where { ExperimentsTable.id eq experimentId }.single(),
            )
        }
    }

    private fun toExperimentResponse(row: ResultRow): ExperimentResponse {
        val experimentId = row[ExperimentsTable.id]
        val metrics = MetricsTable
            .selectAll()
            .where { MetricsTable.experimentId eq experimentId }
            .orderBy(MetricsTable.sortOrder, SortOrder.ASC)
            .map {
                MetricResponse(
                    id = it[MetricsTable.id],
                    name = it[MetricsTable.name],
                    type = it[MetricsTable.type],
                    required = it[MetricsTable.required],
                    sortOrder = it[MetricsTable.sortOrder],
                )
            }
        return ExperimentResponse(
            id = experimentId,
            templateId = row[ExperimentsTable.templateId],
            name = row[ExperimentsTable.name],
            hypothesis = row[ExperimentsTable.hypothesis],
            description = row[ExperimentsTable.description],
            coverColor = row[ExperimentsTable.coverColor],
            startDateMillis = row[ExperimentsTable.startDateMillis],
            durationDays = row[ExperimentsTable.durationDays],
            baselineDays = row[ExperimentsTable.baselineDays],
            interventionDays = row[ExperimentsTable.interventionDays],
            createdAtMillis = row[ExperimentsTable.createdAtMillis],
            updatedAtMillis = row[ExperimentsTable.updatedAtMillis],
            metrics = metrics,
        )
    }

    private fun validate(request: CreateExperimentRequest) {
        if (request.name.isBlank() || request.name.length > 120) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_EXPERIMENT_NAME", "实验名称长度应为 1 到 120 个字符")
        }
        if (request.hypothesis.length > 500 || request.description.length > 5_000) {
            throw ApiException(HttpStatusCode.BadRequest, "EXPERIMENT_TEXT_TOO_LONG", "实验文字内容过长")
        }
        if (request.durationDays !in 1..365 || request.baselineDays < 0 || request.interventionDays < 0 ||
            request.baselineDays + request.interventionDays != request.durationDays
        ) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_SCHEDULE", "基线期与干预期之和必须等于实验总天数")
        }
        if (request.metrics.isEmpty() || request.metrics.size > 20) {
            throw ApiException(HttpStatusCode.BadRequest, "INVALID_METRICS", "实验应包含 1 到 20 个指标")
        }
        request.metrics.forEach { metric ->
            if (metric.name.isBlank() || metric.name.length > 120 || metric.type.uppercase() !in METRIC_TYPES) {
                throw ApiException(HttpStatusCode.BadRequest, "INVALID_METRIC", "指标名称或类型无效")
            }
        }
    }

    private fun writeExperimentFields(
        statement: UpdateBuilder<*>,
        request: CreateExperimentRequest,
        createdAt: Long,
        updatedAt: Long,
    ) {
        statement[ExperimentsTable.templateId] = request.templateId
        statement[ExperimentsTable.name] = request.name.trim()
        statement[ExperimentsTable.hypothesis] = request.hypothesis.trim()
        statement[ExperimentsTable.description] = request.description.trim()
        statement[ExperimentsTable.coverColor] = request.coverColor.trim()
        statement[ExperimentsTable.startDateMillis] = request.startDateMillis
        statement[ExperimentsTable.durationDays] = request.durationDays
        statement[ExperimentsTable.baselineDays] = request.baselineDays
        statement[ExperimentsTable.interventionDays] = request.interventionDays
        statement[ExperimentsTable.createdAtMillis] = createdAt
        statement[ExperimentsTable.updatedAtMillis] = updatedAt
    }

    private fun String?.toUuidOrNew(fieldName: String): String {
        if (this == null) return UUID.randomUUID().toString()
        return runCatching { UUID.fromString(this).toString() }
            .getOrElse {
                throw ApiException(HttpStatusCode.BadRequest, "INVALID_ID", "$fieldName 格式无效")
            }
    }

    companion object {
        private val METRIC_TYPES = setOf("YES_NO", "DURATION", "DECIMAL", "SCORE")
    }
}
