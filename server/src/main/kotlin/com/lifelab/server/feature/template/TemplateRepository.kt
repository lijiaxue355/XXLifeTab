package com.lifelab.server.feature.template

import com.lifelab.server.core.database.DatabaseFactory
import com.lifelab.server.core.database.TemplateMetricsTable
import com.lifelab.server.core.database.TemplatesTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.jdbc.selectAll

class TemplateRepository {
    suspend fun getTemplates(): List<TemplateResponse> = DatabaseFactory.dbQuery {
        TemplatesTable.selectAll().map { template ->
            val templateId = template[TemplatesTable.id]
            TemplateResponse(
                id = templateId,
                name = template[TemplatesTable.name],
                hypothesis = template[TemplatesTable.hypothesis],
                description = template[TemplatesTable.description],
                coverImageUrl = template[TemplatesTable.coverImageUrl],
                durationDays = template[TemplatesTable.durationDays],
                baselineDays = template[TemplatesTable.baselineDays],
                interventionDays = template[TemplatesTable.interventionDays],
                metrics = TemplateMetricsTable
                    .selectAll()
                    .where { TemplateMetricsTable.templateId eq templateId }
                    .orderBy(TemplateMetricsTable.sortOrder, SortOrder.ASC)
                    .map(ResultRow::toTemplateMetric),
            )
        }
    }
}

private fun ResultRow.toTemplateMetric() = TemplateMetricResponse(
    id = this[TemplateMetricsTable.id],
    name = this[TemplateMetricsTable.name],
    type = this[TemplateMetricsTable.type],
    required = this[TemplateMetricsTable.required],
    sortOrder = this[TemplateMetricsTable.sortOrder],
)
