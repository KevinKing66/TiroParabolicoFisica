package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository

class RemoveStudentFromCourseUseCase(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository,
    private val validateRole: ValidateRoleUseCase
) {
    suspend operator fun invoke(courseCode: String, studentCode: String): Result<Unit> {
        val session = authRepository.getCurrentSession() ?: return Result.failure(Exception("Sesión no válida"))
        val role = session.role.lowercase()

        if (!validateRole(listOf("teacher", "admin"))) {
            return Result.failure(Exception("Acceso denegado."))
        }

        val course = courseRepository.getCourse(courseCode).getOrNull() 
            ?: return Result.failure(Exception("Curso no encontrado"))

        if (role == "teacher" && course.ownerId != session.code) {
            return Result.failure(Exception("Solo el propietario puede desvincular estudiantes."))
        }
        
        if (role == "admin" && course.institution != session.institution) {
            return Result.failure(Exception("No tiene permisos sobre esta institución."))
        }

        val updatedStudents = course.studentCodes.filter { it != studentCode }
        return courseRepository.updateCourse(course.copy(studentCodes = updatedStudents))
    }
}
