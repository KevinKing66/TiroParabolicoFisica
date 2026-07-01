package com.king.kevin.tiroparabolico.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.king.kevin.tiroparabolico.core.constants.PhysicsConstants
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.data.repository.AuthSessionStorage
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import kotlinx.coroutines.tasks.await

class AcademicRemoteDataSource(
    private val context: Context,
    private val sessionStorage: AuthSessionStorage
) {
    suspend fun saveAcademicResponse(response: AcademicResponse): Result<Unit> = runCatching {
        val database = getDatabaseOrNull()
            ?: throw IllegalStateException("Firebase no esta configurado.")

        val userCode = sessionStorage.get()?.code ?: "anonymous"
        val reference = database.getReference(PhysicsConstants.ACADEMIC_COLLECTION)

        reference.push().setValue(response.toDto(userCode)).await()
        Unit
    }

    private fun getDatabaseOrNull(): FirebaseDatabase? {
        return runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseDatabase.getInstance()
        }.getOrNull()
    }
}
