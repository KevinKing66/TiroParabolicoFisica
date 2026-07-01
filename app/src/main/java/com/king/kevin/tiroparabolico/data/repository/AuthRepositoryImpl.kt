package com.king.kevin.tiroparabolico.data.repository

import com.king.kevin.tiroparabolico.data.dto.LoginRequestDto
import com.king.kevin.tiroparabolico.data.dto.RegisterRequestDto
import com.king.kevin.tiroparabolico.data.remote.AuthRemoteDataSource
import com.king.kevin.tiroparabolico.domain.model.LoginInput
import com.king.kevin.tiroparabolico.domain.model.RegisterInput
import com.king.kevin.tiroparabolico.domain.model.UserMinimal
import com.king.kevin.tiroparabolico.domain.model.UserSession
import com.king.kevin.tiroparabolico.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

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

    override suspend fun searchStudentsByCode(query: String): Result<List<UserMinimal>> = runCatching {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        // Simplificamos la consulta para evitar requerir un Índice Compuesto manual.
        // Solo filtramos por rol en el servidor.
        val snapshot = firestore.collection("users")
            .whereEqualTo("role", "student")
            .get().await()
        
        // Filtramos por prefijo de código localmente en Kotlin para máxima compatibilidad.
        snapshot.documents.mapNotNull { doc ->
            val code = doc.getString("code") ?: ""
            val fullName = doc.getString("fullname") ?: ""
            
            if (code.startsWith(query, ignoreCase = true)) {
                UserMinimal(code = code, fullName = fullName, role = "student")
            } else null
        }
    }

    override suspend fun createUserWithRole(input: RegisterInput, role: String): Result<Unit> = runCatching {
        remoteDataSource.createUserWithRole(
            RegisterRequestDto(
                fullname = input.fullName.trim(),
                password = input.password,
                email = input.email.trim(),
                institutionName = input.institutionName.trim(),
                courseCode = input.courseCode?.trim() ?: ""
            ),
            role
        )
    }
}
