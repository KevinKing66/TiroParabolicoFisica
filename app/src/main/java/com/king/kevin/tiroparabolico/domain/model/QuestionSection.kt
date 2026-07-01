package com.king.kevin.tiroparabolico.domain.model

data class QuestionSection(
    val id: String,
    val title: String,
    val questions: List<Question>
)
