package com.king.kevin.tiroparabolico.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.king.kevin.tiroparabolico.core.constants.PhysicsConstants
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import kotlinx.coroutines.tasks.await

class AcademicRemoteDataSource(
    private val context: Context
) {
    suspend fun saveAcademicResponse(response: AcademicResponse): Result<Unit> = runCatching {
        val firestore = getFirestoreOrNull()
            ?: throw IllegalStateException("Firebase no esta configurado.")

        firestore.collection(PhysicsConstants.ACADEMIC_COLLECTION)
            .add(response.toDto())
            .await()
        Unit
    }

    private fun getFirestoreOrNull(): FirebaseFirestore? {
        return runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseFirestore.getInstance()
        }.getOrNull()
    }
}
