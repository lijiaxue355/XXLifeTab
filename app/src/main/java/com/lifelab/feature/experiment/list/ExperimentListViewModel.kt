package com.lifelab.feature.experiment.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lifelab.feature.experiment.data.local.relation.ExperimentWithMetrics
import com.lifelab.feature.experiment.data.repository.ExperimentRepository
import com.lifelab.feature.record.data.repository.RecordRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

enum class ExperimentTab {
    ACTIVE,
    COMPLETED,
}

data class ExperimentListItem(
    val experiment: ExperimentWithMetrics,
    val recordedDays: Int,
    val completionPercent: Int,
    val isCompleted: Boolean,
)

data class ExperimentListUiState(
    val selectedTab: ExperimentTab = ExperimentTab.ACTIVE,
    val experiments: List<ExperimentListItem> = emptyList(),
    val activeCount: Int = 0,
    val completedCount: Int = 0,
)

sealed interface ExperimentListEffect {
    data class ShowMessage(
        val message: String,
    ) : ExperimentListEffect
}

class ExperimentListViewModel(
    private val experimentRepository: ExperimentRepository,
    recordRepository: RecordRepository,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(
        ExperimentTab.ACTIVE,
    )

    private val _effect = MutableSharedFlow<ExperimentListEffect>()
    val effect = _effect.asSharedFlow()

    val uiState: StateFlow<ExperimentListUiState> = combine(
        experimentRepository.experiments,
        recordRepository.observeAllRecords(),
        selectedTab,
    ) { experiments, records, tab ->
        val now = System.currentTimeMillis()
        val recordsByExperimentId = records.groupBy {
            it.experimentId
        }

        val items = experiments.map { experimentWithMetrics ->
            val experiment = experimentWithMetrics.experiment
            val recordedDays = recordsByExperimentId[experiment.id]
                .orEmpty()
                .map { it.recordDate }
                .distinct()
                .size
            val totalDays = experiment.durationDays.coerceAtLeast(1)

            ExperimentListItem(
                experiment = experimentWithMetrics,
                recordedDays = recordedDays,
                completionPercent = (
                    recordedDays * 100 / totalDays
                ).coerceIn(0, 100),
                isCompleted = isCompleted(
                    experimentWithMetrics,
                    now,
                ),
            )
        }

        val completedExperiments = items.filter {
            it.isCompleted
        }
        val activeExperiments = items.filterNot {
            it.isCompleted
        }

        ExperimentListUiState(
            selectedTab = tab,
            experiments = when (tab) {
                ExperimentTab.ACTIVE -> activeExperiments
                ExperimentTab.COMPLETED -> completedExperiments
            },
            activeCount = activeExperiments.size,
            completedCount = completedExperiments.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExperimentListUiState(),
    )

    fun selectTab(tab: ExperimentTab) {
        selectedTab.value = tab
    }

    fun deleteExperiment(experimentId: Long) {
        viewModelScope.launch {
            try {
                experimentRepository.deleteExperiment(experimentId)
                _effect.emit(
                    ExperimentListEffect.ShowMessage(
                        "实验已删除",
                    ),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _effect.emit(
                    ExperimentListEffect.ShowMessage(
                        error.message ?: "删除失败",
                    ),
                )
            }
        }
    }

    private fun isCompleted(
        item: ExperimentWithMetrics,
        now: Long,
    ): Boolean {
        val experiment = item.experiment
        val durationMillis = TimeUnit.DAYS.toMillis(
            experiment.durationDays.toLong(),
        )
        val endTime = experiment.startDateMillis + durationMillis
        return now >= endTime
    }

    companion object {
        fun provideFactory(
            experimentRepository: ExperimentRepository,
            recordRepository: RecordRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ExperimentListViewModel(
                    experimentRepository = experimentRepository,
                    recordRepository = recordRepository,
                )
            }
        }
    }
}
