package com.king.kevin.tiroparabolico.data.repository

import com.king.kevin.tiroparabolico.data.remote.AcademicRemoteDataSource
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.repository.AcademicRepository

import kotlinx.coroutines.flow.Flow

class AcademicRepositoryImpl(
    private val remoteDataSource: AcademicRemoteDataSource
) : AcademicRepository {
    override suspend fun saveAcademicResponse(response: AcademicResponse): Result<Unit> {
        return remoteDataSource.saveAcademicResponse(response)
    }

    override fun observeResponsesByLab(labId: String): Flow<Result<List<AcademicResponse>>> {
        return remoteDataSource.observeResponsesByLab(labId)
    }

    override fun observeResponsesByStudentAndLab(studentCode: String, labId: String): Flow<Result<List<AcademicResponse>>> {
        return remoteDataSource.observeResponsesByStudentAndLab(studentCode, labId)
    }
}
