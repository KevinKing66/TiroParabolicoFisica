package com.king.kevin.tiroparabolico.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.king.kevin.tiroparabolico.data.dto.AcademicResponseDto
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.data.repository.AuthSessionStorage
import com.king.kevin.tiroparabolico.domain.model.AcademicResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AcademicRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val sessionStorage: AuthSessionStorage
) {
    suspend fun saveAcademicResponse(response: AcademicResponse): Result<Unit> = runCatching {
        val userCode = sessionStorage.get()?.code ?: "anonymous"
        firestore.collection("academic_responses")
            .add(response.toDto(userCode))
            .await()
        Unit
    }

    fun observeResponsesByLab(labId: String): Flow<Result<List<AcademicResponse>>> = callbackFlow {
        val subscription = firestore.collection("academic_responses")
            .whereEqualTo("labId", labId)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val responses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AcademicResponseDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(Result.success(responses))
            }
        awaitClose { subscription.remove() }
    }
}
