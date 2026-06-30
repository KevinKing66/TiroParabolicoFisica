package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.domain.model.Question
import com.king.kevin.tiroparabolico.domain.repository.LabRepository
import com.king.kevin.tiroparabolico.domain.usecases.AddLabToCourseUseCase
import com.king.kevin.tiroparabolico.presentation.state.CourseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LabViewModel(
    private val addLab: AddLabToCourseUseCase,
    private val labRepository: LabRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseUiState()) // Reusing generic state for simplicity or could create LabUiState
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()

    fun loadLabs(courseCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            labRepository.observeLabsByCourse(courseCode).collect { result ->
                result.onSuccess { labs ->
                    // Here we'd map labs to UI if we had a specific LabUiState
                    _uiState.update { it.copy(isLoading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            }
        }
    }

    fun createLab(code: String, name: String, courseCode: String, questions: List<Question>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val lab = Lab(code, name, courseCode, questions)
            addLab(lab).onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Laboratorio agregado") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
