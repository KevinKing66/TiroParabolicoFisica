package com.king.kevin.tiroparabolico.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.king.kevin.tiroparabolico.data.dto.InstitutionDto
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.domain.model.Institution
import com.king.kevin.tiroparabolico.domain.repository.InstitutionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InstitutionRepositoryImpl(private val firestore: FirebaseFirestore) : InstitutionRepository {
    private val institutionsCollection = firestore.collection("institutions")

    override suspend fun saveInstitution(institution: Institution): Result<Unit> = runCatching {
        institutionsCollection.add(institution.toDto()).await()
    }

    override suspend fun updateInstitution(institution: Institution): Result<Unit> = runCatching {
        institutionsCollection.document(institution.id).set(institution.toDto()).await()
    }

    override suspend fun deleteInstitution(id: String): Result<Unit> = runCatching {
        institutionsCollection.document(id).delete().await()
    }

    override fun observeAllInstitutions(): Flow<Result<List<Institution>>> = callbackFlow {
        val subscription = institutionsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(InstitutionDto::class.java)?.toDomain(it.id) } ?: emptyList()
            trySend(Result.success(list))
        }
        awaitClose { subscription.remove() }
    }
}
