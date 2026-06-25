package com.king.kevin.tiroparabolico.data.dto

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class RegisterRequestDto(
    val fullname: String,
    val password: String,
    val email: String,
    val institutionName: String,
    val courseCode: String
)
