package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.UserSession
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository

class GetCurrentSessionUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): UserSession? = repository.getCurrentSession()
}
