package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.Lab

data class LabDto(
    val code: String = "",
    val name: String = "",
    val courseCode: String = "",
    val questions: List<QuestionDto> = emptyList(),
    val createdAtMillis: Long = 0L
) {
    fun toDomain(): Lab = Lab(
        code = code,
        name = name,
        courseCode = courseCode,
        questions = questions.map { it.toDomain() },
        createdAtMillis = createdAtMillis
    )
}

fun Lab.toDto(): LabDto = LabDto(
    code = code,
    name = name,
    courseCode = courseCode,
    questions = questions.map { it.toDto() },
    createdAtMillis = createdAtMillis
)
