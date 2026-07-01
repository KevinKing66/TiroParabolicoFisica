package com.king.kevin.tiroparabolico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository
import com.king.kevin.tiroparabolico.domain.usecases.CreateCourseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.GetCurrentUserCodeUseCase
import com.king.kevin.tiroparabolico.domain.usecases.UpdateCourseUseCase
import com.king.kevin.tiroparabolico.domain.usecases.ValidateRoleUseCase
import com.king.kevin.tiroparabolico.presentation.state.CourseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CourseViewModel(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository,
    private val createCourse: CreateCourseUseCase,
    private val updateCourse: UpdateCourseUseCase,
    private val validateRole: ValidateRoleUseCase,
    private val getCurrentUserCode: GetCurrentUserCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
        loadCourses()
    }

    private fun checkPermissions() {
        val canManage = validateRole(listOf("teacher", "admin"))
        _uiState.update { it.copy(canManage = canManage) }
    }

    private fun loadCourses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = authRepository.getCurrentSession()
            val role = session?.role?.lowercase() ?: ""
            val userCode = session?.code ?: ""
            val institution = session?.institution ?: ""
            
            val flow = if (role == "admin") {
                courseRepository.observeCoursesByInstitution(institution)
            } else {
                courseRepository.observeCoursesByOwner(userCode)
            }

            flow.collect { result ->
                result.onSuccess { courses ->
                    _uiState.update { it.copy(isLoading = false, courses = courses) }
                }.onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            }
        }
    }

    fun saveCourse(code: String, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val session = authRepository.getCurrentSession()
            val userCode = session?.code ?: ""
            val institution = session?.institution ?: ""

            val toEdit = uiState.value.courseToEdit
            val course = Course(code, name, institution, if (toEdit != null) toEdit.ownerId else userCode)
            
            val result = if (toEdit == null) createCourse(course) else updateCourse(course)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = if (toEdit == null) "Curso creado" else "Curso actualizado", courseToEdit = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Error en operación") }
            }
        }
    }

    fun selectCourseForEdit(course: Course) {
        _uiState.update { it.copy(courseToEdit = course) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(courseToEdit = null) }
    }

    fun deleteCourse(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            courseRepository.deleteCourse(code).onSuccess {
                _uiState.update { it.copy(isLoading = false, successMessage = "Curso eliminado") }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
