package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.model.UserMinimal
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository
import com.king.kevin.tiroparabolico.domain.usecases.AssignStudentToCourseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ValidateRoleUseCase
import com.king.kevin.tiroparabolico.presentation.state.CourseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssignmentUiState(
    val courses: List<Course> = emptyList(),
    val studentsFound: List<UserMinimal> = emptyList(),
    val selectedStudents: List<UserMinimal> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val canManage: Boolean = false
)

class AssignmentViewModel(
    private val assignStudent: AssignStudentToCourseUseCase,
    private val validateRole: ValidateRoleUseCase,
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignmentUiState())
    val uiState: StateFlow<AssignmentUiState> = _uiState.asStateFlow()

    init {
        val allowed = validateRole(listOf("teacher", "admin"))
        _uiState.update { it.copy(canManage = allowed) }
        loadTeacherCourses()
    }

    private fun loadTeacherCourses() {
        val user = authRepository.getCurrentSession() ?: return
        viewModelScope.launch {
            courseRepository.observeCoursesByOwner(user.code).collect { result ->
                result.onSuccess { list ->
                    _uiState.update { it.copy(courses = list) }
                }
            }
        }
    }

    fun searchStudents(query: String) {
        if (query.length < 3) return
        viewModelScope.launch {
            authRepository.searchStudentsByCode(query).onSuccess { list ->
                _uiState.update { it.copy(studentsFound = list) }
            }
        }
    }

    fun toggleStudentSelection(student: UserMinimal) {
        _uiState.update { state ->
            val newList = if (state.selectedStudents.any { it.code == student.code }) {
                state.selectedStudents.filter { it.code != student.code }
            } else {
                state.selectedStudents + student
            }
            state.copy(selectedStudents = newList)
        }
    }

    fun assignMultiple(courseCode: String) {
        val students = uiState.value.selectedStudents
        if (courseCode.isBlank() || students.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Selecciona un curso y al menos un estudiante") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            
            var allSuccess = true
            students.forEach { student ->
                assignStudent(courseCode, student.code).onFailure { allSuccess = false }
            }

            if (allSuccess) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        successMessage = "Estudiantes asignados correctamente",
                        selectedStudents = emptyList()
                    ) 
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Hubo errores en algunas asignaciones") }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
