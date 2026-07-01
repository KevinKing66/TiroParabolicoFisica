package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.RegisterInput
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository

class CreateUserByAdminUseCase(
    private val authRepository: AuthRepository,
    private val validateRole: ValidateRoleUseCase
) {
    suspend operator fun invoke(input: RegisterInput, role: String): Result<Unit> {
        if (!validateRole(listOf("admin"))) {
            return Result.failure(Exception("Permisos insuficientes."))
        }
        
        return authRepository.createUserWithRole(input, role)
    }
}
