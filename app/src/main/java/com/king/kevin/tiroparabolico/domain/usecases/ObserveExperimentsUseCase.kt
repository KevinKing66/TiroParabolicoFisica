package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import com.king.kevin.tiroparabolico.domain.repository.ExperimentRepository
import kotlinx.coroutines.flow.Flow

class ObserveExperimentsUseCase(
    private val repository: ExperimentRepository
) {
    operator fun invoke(): Flow<Result<List<ProjectileExperiment>>> = repository.observeExperiments()
}
