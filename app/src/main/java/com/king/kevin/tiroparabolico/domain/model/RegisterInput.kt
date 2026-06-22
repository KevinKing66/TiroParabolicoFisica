package com.king.kevin.tiroparabolico.domain.model

data class RegisterInput(
    val fullName: String,
    val email: String,
    val password: String,
    val institutionName: String,
    val courseCode: String? = null
)
