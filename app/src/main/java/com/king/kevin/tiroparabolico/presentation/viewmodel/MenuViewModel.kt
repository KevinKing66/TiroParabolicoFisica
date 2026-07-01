package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.domain.model.UserSession
import com.king.kevin.tiroparabolico.domain.repository.AcademicRepository
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository
import com.king.kevin.tiroparabolico.domain.repository.LabRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuUiState(
    val session: UserSession? = null,
    val studentLabs: List<Lab> = emptyList(),
    val isLoading: Boolean = false
)

class MenuViewModel(
    private val authRepository: AuthRepository,
    private val labRepository: LabRepository,
    private val courseRepository: CourseRepository,
    private val academicRepository: AcademicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState(session = authRepository.getCurrentSession()))
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadStudentContent()
    }

    private fun loadStudentContent() {
        val session = uiState.value.session ?: return
        if (session.role.lowercase() == "student") {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                
                courseRepository.observeCoursesByStudent(session.code).collect { result ->
                    result.onSuccess { courses ->
                        val pendingLabs = mutableListOf<Lab>()
                        
                        courses.forEach { course ->
                            labRepository.getLabsByCourse(course.code).onSuccess { labs ->
                                labs.forEach { lab ->
                                    // Comprobar si el alumno ya respondió todas las secciones de este lab
                                    val responses = academicRepository.observeResponsesByStudentAndLab(session.code, lab.code).first().getOrNull() ?: emptyList()
                                    val completedSectionIds = responses.map { it.sectionId }.distinct()
                                    val totalSections = lab.sections.size
                                    
                                    if (completedSectionIds.size < totalSections || totalSections == 0) {
                                        pendingLabs.add(lab)
                                    }
                                    
                                    _uiState.update { it.copy(
                                        isLoading = false, 
                                        studentLabs = pendingLabs.distinctBy { it.code }.sortedByDescending { it.createdAtMillis }
                                    ) }
                                }
                            }
                        }
                        if (courses.isEmpty()) _uiState.update { it.copy(isLoading = false, studentLabs = emptyList()) }
                    }.onFailure {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
