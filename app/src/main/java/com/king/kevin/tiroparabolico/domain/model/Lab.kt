package com.king.kevin.tiroparabolico.domain.model

data class Lab(
    val code: String,
    val name: String,
    val courseCode: String,
    val questions: List<Question> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
)
