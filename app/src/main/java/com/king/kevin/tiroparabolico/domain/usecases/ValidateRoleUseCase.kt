package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.repository.AuthRepository

class ValidateRoleUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(allowedRoles: List<String>): Boolean {
        val session = authRepository.getCurrentSession() ?: return false
        return allowedRoles.contains(session.role.lowercase())
    }
    
    fun getCurrentRole(): String? = authRepository.getCurrentSession()?.role?.lowercase()
}
