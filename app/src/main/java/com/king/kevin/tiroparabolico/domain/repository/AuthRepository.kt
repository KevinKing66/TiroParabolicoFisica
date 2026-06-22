package com.king.kevin.tiroparabolico.domain.repository

import com.king.kevin.tiroparabolico.domain.model.LoginInput
import com.king.kevin.tiroparabolico.domain.model.RegisterInput
import com.king.kevin.tiroparabolico.domain.model.UserSession

interface AuthRepository {
    suspend fun login(input: LoginInput): Result<UserSession>
    suspend fun register(input: RegisterInput): Result<UserSession?>
    fun getCurrentSession(): UserSession?
    fun logout()
}
