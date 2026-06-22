package com.king.kevin.tiroparabolico.domain.repository

import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import kotlinx.coroutines.flow.Flow

interface ExperimentRepository {
    fun observeExperiments(): Flow<Result<List<ProjectileExperiment>>>
    suspend fun saveExperiment(experiment: ProjectileExperiment): Result<Unit>
}
