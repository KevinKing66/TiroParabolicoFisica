package com.king.kevin.tiroparabolico.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.king.kevin.tiroparabolico.data.dto.LabDto
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.domain.model.Lab
import com.king.kevin.tiroparabolico.domain.repository.LabRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class LabRepositoryImpl(private val firestore: FirebaseFirestore) : LabRepository {
    private val labsCollection = firestore.collection("labs")

    override suspend fun saveLab(lab: Lab): Result<Unit> = runCatching {
        labsCollection.document(lab.code).set(lab.toDto()).await()
    }

    override suspend fun updateLab(lab: Lab): Result<Unit> = runCatching {
        labsCollection.document(lab.code).set(lab.toDto()).await()
    }

    override suspend fun deleteLab(code: String): Result<Unit> = runCatching {
        labsCollection.document(code).delete().await()
    }

    override suspend fun getLabsByCourse(courseCode: String): Result<List<Lab>> = runCatching {
        labsCollection.whereEqualTo("courseCode", courseCode).get().await()
            .toObjects(LabDto::class.java).map { it.toDomain() }
    }

    override fun observeLabsByCourse(courseCode: String): Flow<Result<List<Lab>>> = callbackFlow {
        val subscription = labsCollection.whereEqualTo("courseCode", courseCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val labs = snapshot?.toObjects(LabDto::class.java)?.map { it.toDomain() } ?: emptyList()
                trySend(Result.success(labs))
            }
        awaitClose { subscription.remove() }
    }

    override fun observeLabsByInstitution(institution: String): Flow<Result<List<Lab>>> = callbackFlow {
        // This requires an index or we can join with courses. 
        // For simplicity and quality of use, let's assume we search labs across the collection.
        // Actually, LabDto doesn't have institution. We should probably add it or filter through courses.
        // Let's add institution to Lab model/dto for easier admin listing.
        val subscription = labsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val labs = snapshot?.toObjects(LabDto::class.java)?.map { it.toDomain() } ?: emptyList()
            trySend(Result.success(labs))
        }
        awaitClose { subscription.remove() }
    }
}
