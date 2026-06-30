package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository
import com.king.kevin.tiroparabolico.domain.repository.LabRepository

class UpdateCourseUseCase(
    private val courseRepository: CourseRepository,
    private val labRepository: LabRepository,
    private val validateRole: ValidateRoleUseCase,
    private val getCurrentUserCode: GetCurrentUserCodeUseCase
) {
    suspend operator fun invoke(course: Course): Result<Unit> {
        val userCode = getCurrentUserCode() ?: return Result.failure(Exception("Sesión no válida"))
        val isAdmin = validateRole(listOf("admin"))
        
        val existing = courseRepository.getCourse(course.code).getOrNull() 
            ?: return Result.failure(Exception("Curso no encontrado"))

        if (!isAdmin && existing.ownerId != userCode) {
            return Result.failure(Exception("Solo el propietario del curso puede editarlo."))
        }

        // Rule: cannot change code if students or labs exist. 
        // In this implementation, 'code' is the ID. If we are here, 'code' matches existing.
        // We validate if it has students or labs before some specific changes if needed.
        
        return courseRepository.updateCourse(course)
    }
}
