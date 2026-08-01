package com.lifelab.server.feature.template

import kotlinx.serialization.Serializable

@Serializable
data class TemplateResponse(
    val id: String,
    val name: String,
    val hypothesis: String,
    val description: String,
    val coverImageUrl: String,
    val durationDays: Int,
    val baselineDays: Int,
    val interventionDays: Int,
    val metrics: List<TemplateMetricResponse>,
)

@Serializable
data class TemplateMetricResponse(
    val id: String,
    val name: String,
    val type: String,
    val required: Boolean,
    val sortOrder: Int,
)
