package com.king.kevin.tiroparabolico.data.remote

import com.google.firebase.database.FirebaseDatabase
import com.king.kevin.tiroparabolico.data.dto.LoginRequestDto
import com.king.kevin.tiroparabolico.data.dto.RegisterRequestDto
import com.king.kevin.tiroparabolico.domain.model.UserSession
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRemoteDataSource() {

    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    suspend fun login(request: LoginRequestDto): UserSession {
        val snapshot = usersRef.orderByChild("email").equalTo(request.email).get().await()
        
        if (!snapshot.exists()) {
            throw IllegalStateException("Usuario no encontrado.")
        }

        val userSnapshot = snapshot.children.first()
        val storedPassword = userSnapshot.child("password").getValue(String::class.java)

        if (storedPassword != request.password) {
            throw IllegalStateException("Contraseña incorrecta.")
        }

        return UserSession(
            token = userSnapshot.key ?: UUID.randomUUID().toString(),
            fullName = userSnapshot.child("fullname").getValue(String::class.java).orEmpty(),
            code = userSnapshot.child("code").getValue(String::class.java).orEmpty(),
            role = userSnapshot.child("role").getValue(String::class.java) ?: "student",
            course = userSnapshot.child("courseCode").getValue(String::class.java).orEmpty()
        )
    }

    suspend fun register(request: RegisterRequestDto): UserSession {
        // Verificar si ya existe
        val existing = usersRef.orderByChild("email").equalTo(request.email).get().await()
        if (existing.exists()) {
            throw IllegalStateException("El correo electrónico ya está registrado.")
        }

        val userId = usersRef.push().key ?: UUID.randomUUID().toString()
        val userMap = mapOf(
            "fullname" to request.fullname,
            "email" to request.email,
            "password" to request.password,
            "institutionName" to request.institutionName,
            "courseCode" to request.courseCode,
            "code" to (request.courseCode + "-" + (100..999).random()),
            "role" to "student"
        )

        usersRef.child(userId).setValue(userMap).await()

        return UserSession(
            token = userId,
            fullName = request.fullname,
            code = userMap["code"] as String,
            role = "student",
            course = request.courseCode
        )
    }
}
