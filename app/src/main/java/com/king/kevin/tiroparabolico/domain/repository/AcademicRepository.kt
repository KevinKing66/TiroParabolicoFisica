package com.king.kevin.tiroparabolico.domain.repository

import com.king.kevin.tiroparabolico.domain.model.AcademicResponse

import kotlinx.coroutines.flow.Flow

interface AcademicRepository {
    suspend fun saveAcademicResponse(response: AcademicResponse): Result<Unit>
    fun observeResponsesByLab(labId: String): Flow<Result<List<AcademicResponse>>>
}
