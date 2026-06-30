package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository
import com.king.kevin.tiroparabolico.domain.repository.LabRepository

class AddLabToCourseUseCase(
    private val labRepository: LabRepository,
    private val courseRepository: CourseRepository,
    private val validateRole: ValidateRoleUseCase,
    private val getCurrentUserCode: GetCurrentUserCodeUseCase
) {
    suspend operator fun invoke(lab: Lab): Result<Unit> {
        val course = courseRepository.getCourse(lab.courseCode).getOrNull()
            ?: return Result.failure(Exception("Curso no encontrado"))

        val userCode = getCurrentUserCode()
        val isAdmin = validateRole(listOf("admin"))

        if (!isAdmin && course.ownerId != userCode) {
            return Result.failure(Exception("Solo el propietario del curso puede agregar laboratorios."))
        }

        if (lab.code.isBlank() || lab.name.isBlank()) {
            return Result.failure(Exception("Código y nombre del laboratorio son obligatorios."))
        }

        return labRepository.saveLab(lab)
    }
}
