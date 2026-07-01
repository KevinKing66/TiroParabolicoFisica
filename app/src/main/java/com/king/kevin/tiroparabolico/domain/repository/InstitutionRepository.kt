package com.king.kevin.tiroparabolico.domain.repository

import com.king.kevin.tiroparabolico.domain.model.Institution
import kotlinx.coroutines.flow.Flow

interface InstitutionRepository {
    suspend fun saveInstitution(institution: Institution): Result<Unit>
    suspend fun updateInstitution(institution: Institution): Result<Unit>
    suspend fun deleteInstitution(id: String): Result<Unit>
    fun observeAllInstitutions(): Flow<Result<List<Institution>>>
}
