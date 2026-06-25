package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.model.AcademicType

data class AcademicResponseDto(
    val type: String = "",
    val answers: Map<String, String> = emptyMap(),
    val createdAtMillis: Long = 0L
) {
    fun toDomain(id: String): AcademicResponse = AcademicResponse(
        id = id,
        type = AcademicType.valueOf(type),
        answers = answers,
        createdAtMillis = createdAtMillis
    )
}

fun AcademicResponse.toDto(): AcademicResponseDto = AcademicResponseDto(
    type = type.name,
    answers = answers,
    createdAtMillis = createdAtMillis
)
