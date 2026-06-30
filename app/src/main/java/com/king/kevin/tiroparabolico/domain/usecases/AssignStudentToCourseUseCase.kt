package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.repository.CourseRepository

class AssignStudentToCourseUseCase(
    private val courseRepository: CourseRepository,
    private val validateRole: ValidateRoleUseCase,
    private val getCurrentUserCode: GetCurrentUserCodeUseCase
) {
    suspend operator fun invoke(courseCode: String, studentCode: String): Result<Unit> {
        if (!validateRole(listOf("teacher", "admin"))) {
            return Result.failure(Exception("Acceso denegado."))
        }

        val course = courseRepository.getCourse(courseCode).getOrNull() 
            ?: return Result.failure(Exception("Curso no encontrado"))

        val userCode = getCurrentUserCode()
        val isAdmin = validateRole(listOf("admin"))
        
        if (!isAdmin && course.ownerId != userCode) {
            return Result.failure(Exception("Solo el propietario puede asignar estudiantes."))
        }

        if (course.studentCodes.contains(studentCode)) {
            return Result.failure(Exception("El estudiante ya está asignado a este curso."))
        }

        val updatedStudents = course.studentCodes + studentCode
        return courseRepository.updateCourse(course.copy(studentCodes = updatedStudents))
    }
}
