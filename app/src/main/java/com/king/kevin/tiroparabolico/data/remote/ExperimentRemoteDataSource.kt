package com.king.kevin.tiroparabolico.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.king.kevin.tiroparabolico.data.dto.ProjectileExperimentDto
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.data.repository.AuthSessionStorage
import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExperimentRemoteDataSource(
    private val firestore: FirebaseFirestore,
    private val sessionStorage: AuthSessionStorage
) {
    fun observeExperiments(): Flow<Result<List<ProjectileExperiment>>> = callbackFlow {
        val userCode = sessionStorage.get()?.code ?: ""
        val subscription = firestore.collection("experiments")
            .whereEqualTo("userCode", userCode)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val experiments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ProjectileExperimentDto::class.java)?.toDomain(doc.id)
                } ?: emptyList()
                trySend(Result.success(experiments))
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveExperiment(experiment: ProjectileExperiment): Result<Unit> = runCatching {
        val userCode = sessionStorage.get()?.code ?: "anonymous"
        firestore.collection("experiments")
            .add(experiment.toDto(userCode))
            .await()
        Unit
    }
}
