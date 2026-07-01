package com.king.kevin.tiroparabolico.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.king.kevin.tiroparabolico.core.utils.SecurityUtils
import com.king.kevin.tiroparabolico.data.dto.LoginRequestDto
import com.king.kevin.tiroparabolico.data.dto.RegisterRequestDto
import com.king.kevin.tiroparabolico.domain.model.UserSession
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSource(private val firestore: FirebaseFirestore) {

    private val usersCollection = firestore.collection("users")

    suspend fun login(request: LoginRequestDto): UserSession {
        val query = usersCollection.whereEqualTo("email", request.email).get().await()
        
        if (query.isEmpty) {
            throw IllegalStateException("Usuario no encontrado.")
        }

        val userDoc = query.documents.first()
        val storedHash = userDoc.getString("password").orEmpty()

        if (!SecurityUtils.verifyPassword(request.password, storedHash)) {
            throw IllegalStateException("Contraseña incorrecta.")
        }

        return UserSession(
            token = userDoc.id,
            fullName = userDoc.getString("fullname").orEmpty(),
            code = userDoc.getString("code").orEmpty(),
            role = userDoc.getString("role") ?: "student",
            course = userDoc.getString("courseCode").orEmpty(),
            institution = userDoc.getString("institutionName").orEmpty()
        )
    }

    suspend fun register(request: RegisterRequestDto): UserSession {
        // Verificar si ya existe
        val existing = usersCollection.whereEqualTo("email", request.email).get().await()
        if (!existing.isEmpty) {
            throw IllegalStateException("El correo electrónico ya está registrado.")
        }

        // Logic to determine role: if institutionName starts with "PROF_", we'll set as teacher
        val role = if (request.courseCode.startsWith("T-")) "teacher" else "student"
        
        // Generate a unique numeric ID (10 digits)
        val numericId = (1_000_000_000L..9_999_999_999L).random().toString()
        
        val userMap = mutableMapOf(
            "fullname" to request.fullname,
            "email" to request.email,
            "password" to SecurityUtils.hashPassword(request.password),
            "institutionName" to request.institutionName,
            "courseCode" to request.courseCode,
            "code" to numericId,
            "role" to role
        )

        val docRef = usersCollection.document()
        docRef.set(userMap).await()

        return UserSession(
            token = docRef.id,
            fullName = request.fullname,
            code = numericId,
            role = role,
            course = request.courseCode,
            institution = request.institutionName
        )
    }

    suspend fun createUserWithRole(request: RegisterRequestDto, role: String): Unit {
        val existing = usersCollection.whereEqualTo("email", request.email).get().await()
        if (!existing.isEmpty) {
            throw IllegalStateException("El correo electrónico ya está registrado.")
        }

        val numericId = (1_000_000_000L..9_999_999_999L).random().toString()
        
        val userMap = mutableMapOf(
            "fullname" to request.fullname,
            "email" to request.email,
            "password" to SecurityUtils.hashPassword(request.password),
            "institutionName" to request.institutionName,
            "courseCode" to request.courseCode,
            "code" to numericId,
            "role" to role.lowercase()
        )

        usersCollection.document().set(userMap).await()
    }
}
