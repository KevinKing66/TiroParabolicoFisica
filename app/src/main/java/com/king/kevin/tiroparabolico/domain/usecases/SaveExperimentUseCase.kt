package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import com.king.kevin.tiroparabolico.domain.repository.ExperimentRepository
import javax.inject.Inject

class SaveExperimentUseCase @Inject constructor(
    private val repository: ExperimentRepository
) {
    suspend operator fun invoke(experiment: ProjectileExperiment): Result<Unit> = repository.saveExperiment(experiment)
}
