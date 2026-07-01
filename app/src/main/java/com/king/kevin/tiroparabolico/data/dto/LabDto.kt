package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.Lab

data class LabDto(
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val exercise: String = "",
    val courseCode: String = "",
    val sections: List<QuestionSectionDto> = emptyList(),
    val createdAtMillis: Long = 0L
) {
    fun toDomain(): Lab = Lab(
        code = code,
        name = name,
        description = description,
        exercise = exercise,
        courseCode = courseCode,
        sections = sections.map { it.toDomain() },
        createdAtMillis = createdAtMillis
    )
}

fun Lab.toDto(): LabDto = LabDto(
    code = code,
    name = name,
    description = description,
    exercise = exercise,
    courseCode = courseCode,
    sections = sections.map { it.toDto() },
    createdAtMillis = createdAtMillis
)
