package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.Question
import com.king.kevin.tiroparabolico.domain.model.QuestionType

data class QuestionDto(
    val id: String = "",
    val text: String = "",
    val type: String = "TEXT",
    val options: List<String> = emptyList()
) {
    fun toDomain(): Question = Question(
        id = id,
        text = text,
        type = QuestionType.valueOf(type),
        options = options
    )
}

fun Question.toDto(): QuestionDto = QuestionDto(
    id = id,
    text = text,
    type = type.name,
    options = options
)
