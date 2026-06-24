package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.ExperimentInput

class ValidateExperimentInputUseCase() {
    operator fun invoke(input: ExperimentInput): Result<Unit> {
        return when {
            input.initialVelocity <= 0.0 -> Result.failure(IllegalArgumentException("La velocidad inicial debe ser mayor que cero."))
            input.launchAngleDegrees < 0.0 || input.launchAngleDegrees > 90.0 -> {
                Result.failure(IllegalArgumentException("El angulo debe estar entre 0 y 90 grados."))
            }
            input.gravity <= 0.0 -> Result.failure(IllegalArgumentException("La gravedad debe ser positiva."))
            else -> Result.success(Unit)
        }
    }
}
