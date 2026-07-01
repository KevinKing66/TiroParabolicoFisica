package com.king.kevin.tiroparabolico.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.king.kevin.tiroparabolico.data.dto.CourseDto
import com.king.kevin.tiroparabolico.data.dto.toDto
import com.king.kevin.tiroparabolico.domain.model.Course
import com.king.kevin.tiroparabolico.domain.repository.CourseRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CourseRepositoryImpl(private val firestore: FirebaseFirestore) : CourseRepository {
    private val coursesCollection = firestore.collection("courses")

    override suspend fun saveCourse(course: Course): Result<Unit> = runCatching {
        coursesCollection.document(course.code).set(course.toDto()).await()
    }

    override suspend fun updateCourse(course: Course): Result<Unit> = runCatching {
        coursesCollection.document(course.code).set(course.toDto()).await()
    }

    override suspend fun getCourse(code: String): Result<Course?> = runCatching {
        coursesCollection.document(code).get().await().toObject(CourseDto::class.java)?.toDomain()
    }

    override fun observeCoursesByOwner(ownerId: String): Flow<Result<List<Course>>> = callbackFlow {
        val subscription = coursesCollection.whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val courses = snapshot?.toObjects(CourseDto::class.java)?.map { it.toDomain() } ?: emptyList()
                trySend(Result.success(courses))
            }
        awaitClose { subscription.remove() }
    }

    override fun observeCoursesByInstitution(institution: String): Flow<Result<List<Course>>> = callbackFlow {
        val subscription = coursesCollection.whereEqualTo("institution", institution)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val courses = snapshot?.toObjects(CourseDto::class.java)?.map { it.toDomain() } ?: emptyList()
                trySend(Result.success(courses))
            }
        awaitClose { subscription.remove() }
    }

    override fun observeAllCourses(): Flow<Result<List<Course>>> = callbackFlow {
        val subscription = coursesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val courses = snapshot?.toObjects(CourseDto::class.java)?.map { it.toDomain() } ?: emptyList()
            trySend(Result.success(courses))
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun deleteCourse(code: String): Result<Unit> = runCatching {
        coursesCollection.document(code).delete().await()
    }
}
