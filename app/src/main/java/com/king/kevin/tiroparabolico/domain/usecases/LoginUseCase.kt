package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.LoginInput
import com.king.kevin.tiroparabolico.domain.model.UserSession
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository,
    private val validateAuthInput: ValidateAuthInputUseCase
) {
    suspend operator fun invoke(input: LoginInput): Result<UserSession> {
        return validateAuthInput.validateLogin(input).fold(
            onSuccess = { repository.login(input) },
            onFailure = { Result.failure(it) }
        )
    }
}
