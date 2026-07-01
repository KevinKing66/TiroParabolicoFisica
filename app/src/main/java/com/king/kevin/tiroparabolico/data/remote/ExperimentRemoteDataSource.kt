package com.king.kevin.tiroparabolico.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.king.kevin.tiroparabolico.core.constants.PhysicsConstants
import com.king.kevin.tiroparabolico.data.dto.ProjectileExperimentDto
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.data.repository.AuthSessionStorage
import com.king.kevin.tiroparabolico.domain.model.ProjectileExperiment
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExperimentRemoteDataSource(
    private val context: Context,
    private val sessionStorage: AuthSessionStorage
) {
    fun observeExperiments(): Flow<Result<List<ProjectileExperiment>>> = callbackFlow {
        val database = getDatabaseOrNull()
        if (database == null) {
            trySend(Result.failure(IllegalStateException("Firebase no esta configurado.")))
            close()
            return@callbackFlow
        }

        val userCode = sessionStorage.get()?.code ?: ""
        val reference = database.getReference(PhysicsConstants.EXPERIMENT_COLLECTION)

        val query = reference.orderByChild("userCode").equalTo(userCode)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val experiments = snapshot.children.mapNotNull { child ->
                    child.getValue(ProjectileExperimentDto::class.java)?.toDomain(child.key ?: "")
                }.sortedByDescending { it.createdAtMillis }

                trySend(Result.success(experiments))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun saveExperiment(experiment: ProjectileExperiment): Result<Unit> = runCatching {
        val database = getDatabaseOrNull()
            ?: throw IllegalStateException("Firebase no esta configurado.")

        val userCode = sessionStorage.get()?.code ?: "anonymous"
        val reference = database.getReference(PhysicsConstants.EXPERIMENT_COLLECTION)

        reference.push().setValue(experiment.toDto(userCode)).await()
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
