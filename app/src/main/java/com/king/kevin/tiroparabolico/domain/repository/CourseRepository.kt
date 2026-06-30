package com.king.kevin.tiroparabolico.domain.repository

import com.king.kevin.tiroparabolico.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    suspend fun saveCourse(course: Course): Result<Unit>
    suspend fun updateCourse(course: Course): Result<Unit>
    suspend fun getCourse(code: String): Result<Course?>
    fun observeCoursesByOwner(ownerId: String): Flow<Result<List<Course>>>
    fun observeAllCourses(): Flow<Result<List<Course>>>
}
