package com.lifelab.feature.experiment.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lifelab.R
import com.lifelab.feature.experiment.data.mapper.toExperimentEntity
import com.lifelab.feature.experiment.data.mapper.toMetricEntities
import com.lifelab.feature.experiment.data.repository.ExperimentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ExperimentEditorViewModel(private val repository: ExperimentRepository): ViewModel() {
    private val _draft = MutableStateFlow(ExperimentDraft())
    val draft : StateFlow<ExperimentDraft> = _draft.asStateFlow()
    private val _saveState = MutableStateFlow<ExperimentSaveState>(ExperimentSaveState.Idle)
    val saveState : StateFlow<ExperimentSaveState> = _saveState.asStateFlow()


    fun updateBasicInfo(name: String, hypothesis: String ,
                        description: String ){
        _draft.update {
            currentDraft ->
            currentDraft.copy(
                name = name.trim(),
                hypothesis = hypothesis.trim(),
                description = description.trim()
            )
        }
    }
    fun updateSchedule(durationDays: Int){
        val validDuration = durationDays.coerceIn(
            minimumValue = 2,
            maximumValue = 60
        )

        val baselineDays = validDuration / 2
        val interventionDays = validDuration - baselineDays

        _draft.update { currentDraft ->
            currentDraft.copy(
                durationDays = validDuration,
                baselineDays = baselineDays,
                interventionDays = interventionDays
            )
        }
    }

    fun updateMetricName(name: String) {
        _draft.update { currentDraft ->
            currentDraft.copy(
                metrics = listOf(
                    MetricDraft(
                        name = name,
                        type = MetricType.DECIMAL,
                        required = true,
                    ),
                ),
            )
        }
    }

    fun startExperiment(){
        if(_saveState.value is ExperimentSaveState.Saving){
            return
        }
        val currentDraft = _draft.value

        if (currentDraft.metrics.single().name.isBlank()) {
            _saveState.value = ExperimentSaveState.Error(
                message = "请填写观察指标名称",
            )
            return
        }

        viewModelScope.launch {
            try {
                _saveState.value = ExperimentSaveState.Saving

                val experimentId =  repository.createExperiment(experiment = currentDraft.toExperimentEntity(),
                    metrics = currentDraft.toMetricEntities())

                _saveState.value = ExperimentSaveState.Success(experimentId)
            }catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _saveState.value = ExperimentSaveState.Error(
                    message = error.message ?: "未知错误"
                )
            }

        }
    }
    fun consumeSaveError() {
        if (_saveState.value is ExperimentSaveState.Error) {
            _saveState.value = ExperimentSaveState.Idle
        }
    }

    fun resetDraft() {
        _draft.value = ExperimentDraft()
    }
    companion object{
        fun provideFactory(repository: ExperimentRepository) : ViewModelProvider.Factory{
            return viewModelFactory {
                initializer {
                    ExperimentEditorViewModel(repository)
                }
            }
        }
    }


}
