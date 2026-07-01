package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import com.king.kevin.tiroparabolico.domain.model.AcademicType

data class AcademicResponseDto(
    val userCode: String = "",
    val type: String = "",
    val labId: String = "general",
    val sectionId: String = "",
    val answers: Map<String, String> = emptyMap(),
    val createdAtMillis: Long = 0L
) {
    fun toDomain(id: String): AcademicResponse = AcademicResponse(
        id = id,
        userCode = userCode,
        type = AcademicType.valueOf(type),
        labId = labId,
        sectionId = sectionId,
        answers = answers,
        createdAtMillis = createdAtMillis
    )
}

fun AcademicResponse.toDto(userCode: String = ""): AcademicResponseDto = AcademicResponseDto(
    userCode = if (userCode.isBlank()) this.userCode else userCode,
    type = type.name,
    labId = labId,
    sectionId = sectionId,
    answers = answers,
    createdAtMillis = createdAtMillis
)
