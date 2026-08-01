package com.lifelab.server.feature.record

import kotlinx.serialization.Serializable

@Serializable
data class CreateRecordRequest(
    val id: String? = null,
    val experimentId: String,
    val recordedAtMillis: Long,
    val note: String? = null,
    val values: List<CreateRecordValueRequest>,
)

@Serializable
data class CreateRecordValueRequest(
    val metricId: String,
    val value: String,
)

@Serializable
data class RecordResponse(
    val id: String,
    val experimentId: String,
    val recordedAtMillis: Long,
    val note: String? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val values: List<RecordValueResponse>,
)

@Serializable
data class RecordValueResponse(
    val metricId: String,
    val value: String,
)
