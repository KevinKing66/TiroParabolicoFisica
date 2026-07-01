package com.king.kevin.tiroparabolico.presentation.state

import com.king.kevin.tiroparabolico.domain.model.Course

data class CourseUiState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val canManage: Boolean = false,
    val courseToEdit: Course? = null
)
