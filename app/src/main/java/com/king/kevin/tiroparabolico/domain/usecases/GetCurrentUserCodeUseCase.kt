package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.repository.AuthRepository

class GetCurrentUserCodeUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): String? = authRepository.getCurrentSession()?.code
}
