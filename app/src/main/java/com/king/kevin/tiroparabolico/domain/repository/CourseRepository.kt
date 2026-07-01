package com.king.kevin.tiroparabolico.domain.repository

import com.king.kevin.tiroparabolico.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    suspend fun saveCourse(course: Course): Result<Unit>
    suspend fun updateCourse(course: Course): Result<Unit>
    suspend fun getCourse(code: String): Result<Course?>
    fun observeCoursesByOwner(ownerId: String): Flow<Result<List<Course>>>
    fun observeCoursesByStudent(studentCode: String): Flow<Result<List<Course>>>
    fun observeCoursesByInstitution(institution: String): Flow<Result<List<Course>>>
    fun observeAllCourses(): Flow<Result<List<Course>>>
    suspend fun deleteCourse(code: String): Result<Unit>
}
