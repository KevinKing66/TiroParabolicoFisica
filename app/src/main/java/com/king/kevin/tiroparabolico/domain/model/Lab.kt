package com.king.kevin.tiroparabolico.domain.model

data class Lab(
    val code: String,
    val name: String,
    val description: String = "",
    val exercise: String = "",
    val courseCode: String,
    val sections: List<QuestionSection> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
)
