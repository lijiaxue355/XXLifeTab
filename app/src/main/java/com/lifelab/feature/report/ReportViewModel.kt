package com.lifelab.feature.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lifelab.feature.experiment.data.repository.ExperimentRepository
import com.lifelab.feature.record.data.local.entity.DailyRecordEntity
import com.lifelab.feature.record.data.repository.RecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class ReportViewModel(
    private val experimentId: Long,
    private val experimentRepository: ExperimentRepository,
    private val recordRepository: RecordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeReport()
    }

    private fun observeReport() {
        viewModelScope.launch {
            try {
                val experimentWithMetrics =
                    experimentRepository.getExperiment(experimentId)

                if (experimentWithMetrics == null) {
                    _uiState.value = ReportUiState(
                        isLoading = false,
                        message = "实验不存在",
                    )
                    return@launch
                }

                val experiment = experimentWithMetrics.experiment
                val metric = experimentWithMetrics.metrics
                    .sortedBy { it.sortOrder }
                    .firstOrNull { it.type != "YES_NO" }
                    ?: experimentWithMetrics.metrics
                        .minByOrNull { it.sortOrder }

                if (metric == null) {
                    _uiState.value = ReportUiState(
                        isLoading = false,
                        experimentName = experiment.name,
                        totalDays = experiment.durationDays,
                        message = "这个实验还没有观察指标",
                    )
                    return@launch
                }

                recordRepository.observeRecords(experimentId)
                    .collect { records ->
                        val points = records.mapNotNull { record ->
                            val rawValue = recordRepository
                                .decodeValues(record.valuesJson)[metric.id]

                            val value = rawValue
                                ?.let(::parseMetricValue)
                                ?: return@mapNotNull null

                            RecordPoint(
                                record = record,
                                reportPoint = ReportPoint(
                                    label = record.recordDate
                                        .drop(5)
                                        .replace('-', '/'),
                                    value = value,
                                ),
                            )
                        }

                        val baselineValues = points
                            .filter { point ->
                                dayIndex(
                                    recordedAtMillis =
                                        point.record.recordedAtMillis,
                                    startDateMillis =
                                        experiment.startDateMillis,
                                ) < experiment.baselineDays
                            }
                            .map { it.reportPoint.value }

                        val interventionValues = points
                            .filter { point ->
                                dayIndex(
                                    recordedAtMillis =
                                        point.record.recordedAtMillis,
                                    startDateMillis =
                                        experiment.startDateMillis,
                                ) >= experiment.baselineDays
                            }
                            .map { it.reportPoint.value }

                        val recordedDays = records
                            .map { it.recordDate }
                            .distinct()
                            .size
                        val totalDays =
                            experiment.durationDays.coerceAtLeast(1)

                        _uiState.value = ReportUiState(
                            isLoading = false,
                            experimentName = experiment.name,
                            metricName = metric.name,
                            recordedDays = recordedDays,
                            totalDays = totalDays,
                            completionPercent = (
                                recordedDays * 100 / totalDays
                            ).coerceIn(0, 100),
                            linePoints = points.map {
                                it.reportPoint
                            },
                            baselineAverage =
                                baselineValues.averageOrNull(),
                            interventionAverage =
                                interventionValues.averageOrNull(),
                            message = if (points.isEmpty()) {
                                "完成打卡后，这里会显示趋势图"
                            } else {
                                null
                            },
                        )
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value = ReportUiState(
                    isLoading = false,
                    message = error.message ?: "报告加载失败",
                )
            }
        }
    }

    private fun parseMetricValue(value: String): Float? {
        return value.toFloatOrNull() ?: when (
            value.trim().lowercase(Locale.ROOT)
        ) {
            "是", "yes", "true" -> 1f
            "否", "no", "false" -> 0f
            else -> null
        }
    }

    private fun dayIndex(
        recordedAtMillis: Long,
        startDateMillis: Long,
    ): Int {
        return TimeUnit.MILLISECONDS.toDays(
            (recordedAtMillis - startDateMillis)
                .coerceAtLeast(0L),
        ).toInt()
    }

    private fun List<Float>.averageOrNull(): Float? {
        return if (isEmpty()) null else average().toFloat()
    }

    private data class RecordPoint(
        val record: DailyRecordEntity,
        val reportPoint: ReportPoint,
    )

    companion object {
        fun provideFactory(
            experimentId: Long,
            experimentRepository: ExperimentRepository,
            recordRepository: RecordRepository,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ReportViewModel(
                        experimentId = experimentId,
                        experimentRepository = experimentRepository,
                        recordRepository = recordRepository,
                    )
                }
            }
        }
    }
}
