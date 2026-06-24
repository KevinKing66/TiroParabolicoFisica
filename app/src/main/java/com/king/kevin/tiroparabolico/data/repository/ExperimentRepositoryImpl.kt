package com.king.kevin.tiroparabolico.data.repository

import com.king.kevin.tiroparabolico.data.remote.ExperimentRemoteDataSource
import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import com.king.kevin.tiroparabolico.domain.repository.ExperimentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class ExperimentRepositoryImpl(
    private val remoteDataSource: ExperimentRemoteDataSource
) : ExperimentRepository {
    override fun observeExperiments(): Flow<Result<List<ProjectileExperiment>>> {
        return remoteDataSource.observeExperiments().flowOn(Dispatchers.IO)
    }

    override suspend fun saveExperiment(experiment: ProjectileExperiment): Result<Unit> {
        return remoteDataSource.saveExperiment(experiment)
    }
}
