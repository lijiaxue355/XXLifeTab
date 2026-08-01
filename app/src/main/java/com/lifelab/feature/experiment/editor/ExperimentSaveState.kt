package com.lifelab.feature.experiment.editor

sealed interface ExperimentSaveState {
    data object Idle : ExperimentSaveState
    data object Saving : ExperimentSaveState
    data class Success(val experimentId: Long) : ExperimentSaveState
    data class Error(
        val message: String
    ) : ExperimentSaveState
}