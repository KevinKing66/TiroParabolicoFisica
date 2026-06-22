package com.king.kevin.tiroparabolico.presentation.state

import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment

data class ExperimentUiState(
    val currentExperiment: ProjectileExperiment? = null,
    val history: List<ProjectileExperiment> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
