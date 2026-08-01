package com.lifelab.server.feature.experiment

import kotlinx.serialization.Serializable

@Serializable
data class CreateExperimentRequest(
    val id: String? = null,
    val templateId: String? = null,
    val name: String,
    val hypothesis: String,
    val description: String = "",
    val coverColor: String = "purple",
    val startDateMillis: Long,
    val durationDays: Int,
    val baselineDays: Int,
    val interventionDays: Int,
    val metrics: List<CreateMetricRequest>,
)

@Serializable
data class CreateMetricRequest(
    val id: String? = null,
    val name: String,
    val type: String,
    val required: Boolean = true,
    val sortOrder: Int,
)

@Serializable
data class ExperimentResponse(
    val id: String,
    val templateId: String? = null,
    val name: String,
    val hypothesis: String,
    val description: String,
    val coverColor: String,
    val startDateMillis: Long,
    val durationDays: Int,
    val baselineDays: Int,
    val interventionDays: Int,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val metrics: List<MetricResponse>,
)

@Serializable
data class MetricResponse(
    val id: String,
    val name: String,
    val type: String,
    val required: Boolean,
    val sortOrder: Int,
)
