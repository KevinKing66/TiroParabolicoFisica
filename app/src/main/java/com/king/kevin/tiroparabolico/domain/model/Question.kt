package com.king.kevin.tiroparabolico.domain.model

enum class QuestionType {
    TEXT,
    NUMERIC,
    MULTIPLE_CHOICE
}

data class Question(
    val id: String,
    val text: String,
    val type: QuestionType,
    val options: List<String> = emptyList()
)
