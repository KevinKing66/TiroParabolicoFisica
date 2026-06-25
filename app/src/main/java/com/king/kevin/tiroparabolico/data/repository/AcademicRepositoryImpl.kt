package com.king.kevin.tiroparabolico.data.repository

import com.king.kevin.tiroparabolico.data.remote.AcademicRemoteDataSource
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.repository.AcademicRepository

class AcademicRepositoryImpl(
    private val remoteDataSource: AcademicRemoteDataSource
) : AcademicRepository {
    override suspend fun saveAcademicResponse(response: AcademicResponse): Result<Unit> {
        return remoteDataSource.saveAcademicResponse(response)
    }
}
