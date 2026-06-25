package com.king.kevin.tiroparabolico.domain.repository

import com.king.kevin.tiroparabolico.domain.model.AcademicResponse

interface AcademicRepository {
    suspend fun saveAcademicResponse(response: AcademicResponse): Result<Unit>
}
