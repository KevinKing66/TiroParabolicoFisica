package com.king.kevin.tiroparabolico.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.king.kevin.tiroparabolico.core.constants.PhysicsConstants
import com.king.kevin.tiroparabolico.data.dto.ProjectileExperimentDto
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExperimentRemoteDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun observeExperiments(): Flow<Result<List<ProjectileExperiment>>> = callbackFlow {
        val firestore = getFirestoreOrNull()
        if (firestore == null) {
            trySend(Result.failure(IllegalStateException("Firebase no esta configurado. Agrega google-services.json para persistencia remota.")))
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(PhysicsConstants.EXPERIMENT_COLLECTION)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    trySend(Result.failure(exception))
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents.orEmpty()
                val experiments = documents.mapNotNull { document ->
                    document.toObject(ProjectileExperimentDto::class.java)?.toDomain(document.id)
                }
                trySend(Result.success(experiments))
            }

        awaitClose { registration.remove() }
    }

    suspend fun saveExperiment(experiment: ProjectileExperiment): Result<Unit> = runCatching {
        val firestore = getFirestoreOrNull()
            ?: throw IllegalStateException("Firebase no esta configurado. Agrega google-services.json para guardar experimentos.")

        firestore.collection(PhysicsConstants.EXPERIMENT_COLLECTION)
            .add(experiment.toDto())
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
