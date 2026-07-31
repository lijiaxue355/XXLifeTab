package com.lifelab.feature.experiment.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lifelab.R
import com.lifelab.feature.experiment.data.repository.ExperimentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExperimentEditorViewModel(private val repository: ExperimentRepository): ViewModel() {
    private val _draft = MutableStateFlow(ExperimentDraft())
    val draft : StateFlow<ExperimentDraft> = _draft.asStateFlow()


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