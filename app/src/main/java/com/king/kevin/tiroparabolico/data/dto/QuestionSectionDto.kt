package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.QuestionSection

data class QuestionSectionDto(
    val id: String = "",
    val title: String = "",
    val questions: List<QuestionDto> = emptyList()
) {
    fun toDomain(): QuestionSection = QuestionSection(
        id = id,
        title = title,
        questions = questions.map { it.toDomain() }
    )
}

fun QuestionSection.toDto(): QuestionSectionDto = QuestionSectionDto(
    id = id,
    title = title,
    questions = questions.map { it.toDto() }
)
