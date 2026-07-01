package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.domain.model.QuestionSection
import com.king.kevin.tiroparabolico.domain.repository.AcademicRepository
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository
import com.king.kevin.tiroparabolico.domain.repository.LabRepository
import com.king.kevin.tiroparabolico.domain.usecases.AddLabToCourseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LabUiState(
    val availableCourses: List<Course> = emptyList(),
    val labs: List<Lab> = emptyList(),
    val labResponses: List<AcademicResponse> = emptyList(),
    val labToEdit: Lab? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class LabViewModel(
    private val addLab: AddLabToCourseUseCase,
    private val labRepository: LabRepository,
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository,
    private val academicRepository: AcademicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabUiState())
    val uiState: StateFlow<LabUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val session = authRepository.getCurrentSession() ?: return
        val role = session.role.lowercase()
        
        viewModelScope.launch {
            val courseFlow = if (role == "admin") {
                courseRepository.observeCoursesByInstitution(session.institution)
            } else {
                courseRepository.observeCoursesByOwner(session.code)
            }
            
            courseFlow.collect { result ->
                result.onSuccess { list -> 
                    _uiState.update { it.copy(availableCourses = list) }
                    if (list.isNotEmpty()) {
                        observeLabsForCourses(list.map { it.code })
                    }
                }
            }
        }
    }

    private fun observeLabsForCourses(courseCodes: List<String>) {
        viewModelScope.launch {
            courseCodes.forEach { code ->
                labRepository.observeLabsByCourse(code).collect { result ->
                    result.onSuccess { newLabs ->
                        _uiState.update { state ->
                            val currentLabs = state.labs.toMutableList()
                            newLabs.forEach { lab ->
                                val index = currentLabs.indexOfFirst { it.code == lab.code }
                                if (index >= 0) currentLabs[index] = lab else currentLabs.add(lab)
                            }
                            state.copy(labs = currentLabs.sortedByDescending { it.createdAtMillis })
                        }
                    }
                }
            }
        }
    }

    fun selectLabForEdit(lab: Lab) {
        _uiState.update { it.copy(labToEdit = lab) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(labToEdit = null) }
    }

    fun deleteLab(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            labRepository.deleteLab(code).onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Laboratorio eliminado") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun observeLabResponses(labCode: String) {
        viewModelScope.launch {
            academicRepository.observeResponsesByLab(labCode).collect { result ->
                result.onSuccess { list ->
                    _uiState.update { it.copy(labResponses = list) }
                }
            }
        }
    }

    fun createLab(
        code: String,
        name: String,
        description: String,
        exercise: String,
        courseCode: String,
        sections: List<QuestionSection>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val lab = Lab(
                code = code,
                name = name,
                description = description,
                exercise = exercise,
                courseCode = courseCode,
                sections = sections
            )
            
            val isEdit = uiState.value.labToEdit != null
            val result = if (isEdit) labRepository.updateLab(lab) else addLab(lab)

            result.onSuccess {
                _uiState.update { it.copy(
                    isLoading = false, 
                    successMessage = if (isEdit) "Laboratorio actualizado" else "Laboratorio creado",
                    labToEdit = null
                ) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Error en operación") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
