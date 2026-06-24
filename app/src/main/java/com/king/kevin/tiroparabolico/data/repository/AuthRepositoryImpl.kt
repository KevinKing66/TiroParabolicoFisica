package com.king.kevin.tiroparabolico.data.repository

import com.king.kevin.tiroparabolico.data.dto.LoginRequestDto
import com.king.kevin.tiroparabolico.data.dto.RegisterRequestDto
import com.king.kevin.tiroparabolico.data.remote.AuthRemoteDataSource
import com.king.kevin.tiroparabolico.data.remote.AuthResponseParser
import com.king.kevin.tiroparabolico.data.remote.JwtParser
import com.king.kevin.tiroparabolico.domain.model.LoginInput
import com.king.kevin.tiroparabolico.domain.model.RegisterInput
import com.king.kevin.tiroparabolico.domain.model.UserSession
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val responseParser: AuthResponseParser,
    private val jwtParser: JwtParser,
    private val sessionStorage: AuthSessionStorage
) : AuthRepository {
    override suspend fun login(input: LoginInput): Result<UserSession> = runCatching {
        val response = remoteDataSource.login(LoginRequestDto(input.email.trim(), input.password))
        val token = responseParser.extractToken(response)
            ?: throw IllegalStateException("El servidor no devolvio un token JWT.")
        jwtParser.parse(token).also(sessionStorage::save)
    }

    override suspend fun register(input: RegisterInput): Result<UserSession?> = runCatching {
        val response = remoteDataSource.register(
            RegisterRequestDto(
                fullname = input.fullName.trim(),
                password = input.password,
                email = input.email.trim(),
                institutionName = input.institutionName.trim(),
                courseCode = input.courseCode?.trim()
            )
        )
        responseParser.extractToken(response)
            ?.let(jwtParser::parse)
            ?.also(sessionStorage::save)
    }

    override fun getCurrentSession(): UserSession? = sessionStorage.get()

    override fun logout() {
        sessionStorage.clear()
    }
}
