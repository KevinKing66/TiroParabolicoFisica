package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.core.constants.PhysicsConstants
import com.king.kevin.tiroparabolico.domain.model.ExperimentInput
import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import com.king.kevin.tiroparabolico.domain.usecases.CalculateProjectileExperimentUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ObserveExperimentsUseCase
import com.king.kevin.tiroparabolico.domain.usecases.SaveExperimentUseCase
import com.king.kevin.tiroparabolico.presentation.state.ExperimentUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperimentViewModel @Inject constructor(
    private val calculateProjectileExperiment: CalculateProjectileExperimentUseCase,
    private val saveExperiment: SaveExperimentUseCase,
    observeExperiments: ObserveExperimentsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExperimentUiState())
    val uiState: StateFlow<ExperimentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeExperiments().collect { result ->
                result
                    .onSuccess { history -> _uiState.update { it.copy(history = history) } }
                    .onFailure { error -> showError(error.message.orEmpty()) }
            }
        }
    }

    fun simulate(initialVelocity: Double?, angleDegrees: Double?, gravity: Double?) {
        if (initialVelocity == null || angleDegrees == null) {
            showError("Ingresa velocidad inicial y angulo de lanzamiento.")
            return
        }

        val input = ExperimentInput(
            initialVelocity = initialVelocity,
            launchAngleDegrees = angleDegrees,
            gravity = gravity ?: PhysicsConstants.EARTH_GRAVITY
        )

        calculateProjectileExperiment(input)
            .onSuccess { experiment ->
                _uiState.update {
                    it.copy(
                        currentExperiment = experiment,
                        errorMessage = null,
                        successMessage = "Simulacion calculada correctamente."
                    )
                }
            }
            .onFailure { error -> showError(error.message.orEmpty()) }
    }

    fun saveCurrentExperiment() {
        val experiment = uiState.value.currentExperiment
        if (experiment == null) {
            showError("Primero ejecuta una simulacion.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            saveExperiment(experiment)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, successMessage = "Experimento guardado en Firestore.")
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "No fue posible guardar el experimento."
                        )
                    }
                }
        }
    }

    fun selectExperiment(experiment: ProjectileExperiment) {
        _uiState.update {
            it.copy(currentExperiment = experiment, errorMessage = null, successMessage = "Experimento cargado desde el historial.")
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(errorMessage = message.ifBlank { "Verifica los datos ingresados." }, successMessage = null)
        }
    }
}
