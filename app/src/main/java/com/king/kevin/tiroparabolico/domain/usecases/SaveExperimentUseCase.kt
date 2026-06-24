package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import com.king.kevin.tiroparabolico.domain.repository.ExperimentRepository

class SaveExperimentUseCase(
    private val repository: ExperimentRepository
) {
    suspend operator fun invoke(experiment: ProjectileExperiment): Result<Unit> = repository.saveExperiment(experiment)
}
