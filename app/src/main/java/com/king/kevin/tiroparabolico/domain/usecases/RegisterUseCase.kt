package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.RegisterInput
import com.king.kevin.tiroparabolico.domain.model.UserSession
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val validateAuthInput: ValidateAuthInputUseCase
) {
    suspend operator fun invoke(input: RegisterInput): Result<UserSession?> {
        return validateAuthInput.validateRegister(input).fold(
            onSuccess = { repository.register(input) },
            onFailure = { Result.failure(it) }
        )
    }
}
