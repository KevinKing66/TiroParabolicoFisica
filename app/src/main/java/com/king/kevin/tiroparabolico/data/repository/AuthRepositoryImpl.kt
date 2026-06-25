package com.king.kevin.tiroparabolico.data.repository

import com.king.kevin.tiroparabolico.data.dto.LoginRequestDto
import com.king.kevin.tiroparabolico.data.dto.RegisterRequestDto
import com.king.kevin.tiroparabolico.data.remote.AuthRemoteDataSource
import com.king.kevin.tiroparabolico.domain.model.LoginInput
import com.king.kevin.tiroparabolico.domain.model.RegisterInput
import com.king.kevin.tiroparabolico.domain.model.UserSession
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionStorage: AuthSessionStorage
) : AuthRepository {
    override suspend fun login(input: LoginInput): Result<UserSession> = runCatching {
        remoteDataSource.login(LoginRequestDto(input.email.trim(), input.password))
            .also(sessionStorage::save)
    }

    override suspend fun register(input: RegisterInput): Result<UserSession?> = runCatching {
        remoteDataSource.register(
            RegisterRequestDto(
                fullname = input.fullName.trim(),
                password = input.password,
                email = input.email.trim(),
                institutionName = input.institutionName.trim(),
                courseCode = input.courseCode?.trim() ?: ""
            )
        ).also(sessionStorage::save)
    }

    override fun getCurrentSession(): UserSession? = sessionStorage.get()

    override fun logout() {
        sessionStorage.clear()
    }
}
