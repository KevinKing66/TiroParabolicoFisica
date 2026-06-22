package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import com.king.kevin.tiroparabolico.domain.repository.ExperimentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExperimentsUseCase @Inject constructor(
    private val repository: ExperimentRepository
) {
    operator fun invoke(): Flow<Result<List<ProjectileExperiment>>> = repository.observeExperiments()
}
