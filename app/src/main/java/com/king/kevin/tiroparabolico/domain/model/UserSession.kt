package com.king.kevin.tiroparabolico.domain.model

data class UserSession(
    val token: String,
    val fullName: String,
    val code: String,
    val role: String,
    val course: String,
    val institution: String = ""
)
