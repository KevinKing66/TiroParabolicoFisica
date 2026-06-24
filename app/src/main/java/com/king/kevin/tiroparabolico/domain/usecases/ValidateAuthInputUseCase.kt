package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.LoginInput
import com.king.kevin.tiroparabolico.domain.model.RegisterInput

class ValidateAuthInputUseCase() {
    fun validateLogin(input: LoginInput): Result<Unit> {
        return when {
            input.email.isBlank() -> Result.failure(IllegalArgumentException("El email es obligatorio."))
            !input.email.isValidEmail() -> Result.failure(IllegalArgumentException("Ingresa un email valido."))
            input.password.isBlank() -> Result.failure(IllegalArgumentException("La contrasena es obligatoria."))
            input.password.length < 6 -> Result.failure(IllegalArgumentException("La contrasena debe tener al menos 6 caracteres."))
            else -> Result.success(Unit)
        }
    }

    fun validateRegister(input: RegisterInput): Result<Unit> {
        return when {
            input.fullName.isBlank() -> Result.failure(IllegalArgumentException("El nombre completo es obligatorio."))
            input.email.isBlank() -> Result.failure(IllegalArgumentException("El email es obligatorio."))
            !input.email.isValidEmail() -> Result.failure(IllegalArgumentException("Ingresa un email valido."))
            input.password.isBlank() -> Result.failure(IllegalArgumentException("La contrasena es obligatoria."))
            input.password.length < 6 -> Result.failure(IllegalArgumentException("La contrasena debe tener al menos 6 caracteres."))
            input.institutionName.isBlank() -> Result.failure(IllegalArgumentException("El nombre de la institucion es obligatorio."))
            else -> Result.success(Unit)
        }
    }

    private fun String.isValidEmail(): Boolean {
        return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$").matches(trim())
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
