package com.lifelab.feature.experiment.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lifelab.feature.experiment.data.local.relation.ExperimentWithMetrics
import com.lifelab.feature.experiment.data.repository.ExperimentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExperimentListViewModel(
    repository: ExperimentRepository,
) : ViewModel() {

    val experiments: StateFlow<List<ExperimentWithMetrics>> =
        repository.experiments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    companion object {
        fun provideFactory(repository: ExperimentRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ExperimentListViewModel(repository)
                }
            }
    }
}
