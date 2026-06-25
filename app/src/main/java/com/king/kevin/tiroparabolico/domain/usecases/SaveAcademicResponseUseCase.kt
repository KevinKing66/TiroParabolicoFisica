package com.king.kevin.tiroparabolico.domain.usecases

import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.repository.AcademicRepository

class SaveAcademicResponseUseCase(
    private val repository: AcademicRepository
) {
    suspend operator fun invoke(response: AcademicResponse): Result<Unit> {
        return repository.saveAcademicResponse(response)
    }
}
