package com.lifelab.core.sync.data.remote

data class UploadExperimentRequestDto(
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
    val metrics: List<UploadMetricRequestDto>,
)

data class UploadMetricRequestDto(
    val id: String,
    val name: String,
    val type: String,
    val required: Boolean,
    val sortOrder: Int,
)

data class UploadRecordRequestDto(
    val id: String,
    val experimentId: String,
    val recordedAtMillis: Long,
    val note: String?,
    val values: List<UploadRecordValueRequestDto>,
)

data class UploadRecordValueRequestDto(
    val metricId: String,
    val value: String,
)

data class SyncResponseDto(
    val id: String,
)

data class RemoteExperimentDto(
    val id: String,
    val name: String,
    val hypothesis: String,
    val description: String,
    val coverColor: String,
    val startDateMillis: Long,
    val durationDays: Int,
    val baselineDays: Int,
    val interventionDays: Int,
    val createdAtMillis: Long,
    val metrics: List<RemoteMetricDto>,
)

data class RemoteMetricDto(
    val id: String,
    val name: String,
    val type: String,
    val required: Boolean,
    val sortOrder: Int,
)

data class RemoteRecordDto(
    val id: String,
    val experimentId: String,
    val recordedAtMillis: Long,
    val note: String?,
    val values: List<RemoteRecordValueDto>,
)

data class RemoteRecordValueDto(
    val metricId: String,
    val value: String,
)
