package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository

class CreateCourseUseCase(
    private val courseRepository: CourseRepository,
    private val validateRole: ValidateRoleUseCase
) {
    suspend operator fun invoke(course: Course): Result<Unit> {
        if (!validateRole(listOf("teacher", "admin"))) {
            return Result.failure(Exception("Permisos insuficientes para crear cursos."))
        }
        
        if (course.code.isBlank() || course.name.isBlank()) {
            return Result.failure(Exception("El código y nombre son obligatorios."))
        }

        // Check if course already exists
        val existing = courseRepository.getCourse(course.code).getOrNull()
        if (existing != null) {
            return Result.failure(Exception("Ya existe un curso con el código ${course.code}"))
        }

        return courseRepository.saveCourse(course)
    }
}
