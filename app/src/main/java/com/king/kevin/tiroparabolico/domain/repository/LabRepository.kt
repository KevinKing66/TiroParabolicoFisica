package com.king.kevin.tiroparabolico.domain.repository

import com.king.kevin.tiroparabolico.domain.model.Lab
import kotlinx.coroutines.flow.Flow

interface LabRepository {
    suspend fun saveLab(lab: Lab): Result<Unit>
    suspend fun updateLab(lab: Lab): Result<Unit>
    suspend fun deleteLab(code: String): Result<Unit>
    suspend fun getLabsByCourse(courseCode: String): Result<List<Lab>>
    fun observeLabsByCourse(courseCode: String): Flow<Result<List<Lab>>>
    fun observeLabsByInstitution(institution: String): Flow<Result<List<Lab>>>
}
