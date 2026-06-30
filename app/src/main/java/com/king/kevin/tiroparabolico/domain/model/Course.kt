package com.king.kevin.tiroparabolico.domain.model

data class Course(
    val code: String,
    val name: String,
    val institution: String,
    val ownerId: String,
    val studentCodes: List<String> = emptyList()
)
