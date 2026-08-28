package com.lifelab.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lifelab.feature.experiment.data.repository.ExperimentRepository
import com.lifelab.feature.record.data.repository.RecordRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class TodayViewModel(
    private val experimentRepository: ExperimentRepository,
    private val recordRepository: RecordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<TodayUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        observeToday()
    }

    fun dispatch(action: TodayUiAction) {
        when (action) {
            is TodayUiAction.SaveRecordClicked -> {
                saveRecord(
                    experimentId = action.experimentId,
                    values = action.values,
                    note = action.note,
                )
            }
        }
    }

    private fun saveRecord(
        experimentId: Long,
        values: Map<Long, String>,
        note: String?,
    ) {
        val item = _uiState.value.items.firstOrNull {
            it.experiment.experiment.id == experimentId
        }

        if (item == null) {
            showMessage("实验不存在")
            return
        }

        val missingMetric = item.experiment.metrics.firstOrNull { metric ->
            metric.required && values[metric.id].isNullOrBlank()
        }

        if (missingMetric != null) {
            showMessage("请填写${missingMetric.name}")
            return
        }

        if (item.isSaving) {
            return
        }

        viewModelScope.launch {
            setSaving(experimentId, true)

            try {
                recordRepository.saveTodayRecord(
                    experimentId = experimentId,
                    values = values,
                    note = note,
                )

                _uiEffect.emit(
                    TodayUiEffect.ShowMessage(
                        "已保存，正在同步",
                    ),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiEffect.emit(
                    TodayUiEffect.ShowMessage(
                        error.message ?: "保存失败",
                    ),
                )
            } finally {
                setSaving(experimentId, false)
            }
        }
    }

    private fun observeToday() {
        viewModelScope.launch {
            combine(
                experimentRepository.experiments,
                recordRepository.observeTodayRecords(),
            ) { experiments, records ->
                val recordsByExperimentId = records.associateBy {
                    it.experimentId
                }
                val savingExperimentIds = _uiState.value.items
                    .filter { it.isSaving }
                    .map { it.experiment.experiment.id }
                    .toSet()

                TodayUiState(
                    items = experiments.map { experiment ->
                        val experimentId = experiment.experiment.id
                        val record = recordsByExperimentId[experimentId]

                        TodayExperimentItem(
                            experiment = experiment,
                            values = record?.let {
                                recordRepository.decodeValues(
                                    it.valuesJson,
                                )
                            }.orEmpty(),
                            note = record?.note.orEmpty(),
                            hasRecord = record != null,
                            syncStatus = record?.syncStatus,
                            isSaving = experimentId in savingExperimentIds,
                        )
                    },
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun setSaving(
        experimentId: Long,
        isSaving: Boolean,
    ) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (
                        item.experiment.experiment.id == experimentId
                    ) {
                        item.copy(isSaving = isSaving)
                    } else {
                        item
                    }
                },
            )
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(
                TodayUiEffect.ShowMessage(message),
            )
        }
    }

    companion object {
        fun provideFactory(
            experimentRepository: ExperimentRepository,
            recordRepository: RecordRepository,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    TodayViewModel(
                        experimentRepository = experimentRepository,
                        recordRepository = recordRepository,
                    )
                }
            }
        }
    }
}
