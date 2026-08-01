package com.lifelab.server.core.database

import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

internal fun seedTemplates() {
    if (TemplatesTable.selectAll().count() > 0) return

    val sleepTemplateId = "11111111-1111-4111-8111-111111111111"
    val walkTemplateId = "22222222-2222-4222-8222-222222222222"

    TemplatesTable.insert {
        it[id] = sleepTemplateId
        it[name] = "睡前一小时不看手机"
        it[hypothesis] = "减少睡前屏幕刺激可以缩短入睡时间并提升晨间精力"
        it[description] = "连续记录睡前手机使用、入睡耗时、睡眠时长和晨间精力。"
        it[coverImageUrl] = "https://images.unsplash.com/photo-1511295742362-92c96b1cf484"
        it[durationDays] = 14
        it[baselineDays] = 7
        it[interventionDays] = 7
    }
    TemplatesTable.insert {
        it[id] = walkTemplateId
        it[name] = "每天步行 30 分钟"
        it[hypothesis] = "稳定的日常步行能够改善情绪和白天精力"
        it[description] = "记录步行时长、完成情况、情绪和精力变化。"
        it[coverImageUrl] = "https://images.unsplash.com/photo-1551632811-561732d1e306"
        it[durationDays] = 21
        it[baselineDays] = 7
        it[interventionDays] = 14
    }

    listOf(
        TemplateMetricSeed("11111111-1111-4111-8111-111111111101", sleepTemplateId, "是否完成干预", "YES_NO", 0),
        TemplateMetricSeed("11111111-1111-4111-8111-111111111102", sleepTemplateId, "入睡耗时", "DURATION", 1),
        TemplateMetricSeed("11111111-1111-4111-8111-111111111103", sleepTemplateId, "睡眠时长", "DECIMAL", 2),
        TemplateMetricSeed("11111111-1111-4111-8111-111111111104", sleepTemplateId, "晨间精力", "SCORE", 3),
        TemplateMetricSeed("22222222-2222-4222-8222-222222222201", walkTemplateId, "是否完成步行", "YES_NO", 0),
        TemplateMetricSeed("22222222-2222-4222-8222-222222222202", walkTemplateId, "步行时长", "DURATION", 1),
        TemplateMetricSeed("22222222-2222-4222-8222-222222222203", walkTemplateId, "今日情绪", "SCORE", 2),
        TemplateMetricSeed("22222222-2222-4222-8222-222222222204", walkTemplateId, "白天精力", "SCORE", 3),
    ).forEach { metric ->
        TemplateMetricsTable.insert {
            it[id] = metric.id
            it[templateId] = metric.templateId
            it[name] = metric.name
            it[type] = metric.type
            it[required] = true
            it[sortOrder] = metric.sortOrder
        }
    }
}

private data class TemplateMetricSeed(
    val id: String,
    val templateId: String,
    val name: String,
    val type: String,
    val sortOrder: Int,
)
