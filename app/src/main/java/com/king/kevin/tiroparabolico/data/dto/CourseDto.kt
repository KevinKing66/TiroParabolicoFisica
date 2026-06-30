package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.Course

data class CourseDto(
    val code: String = "",
    val name: String = "",
    val institution: String = "",
    val ownerId: String = "",
    val studentCodes: List<String> = emptyList()
) {
    fun toDomain(): Course = Course(
        code = code,
        name = name,
        institution = institution,
        ownerId = ownerId,
        studentCodes = studentCodes
    )
}

fun Course.toDto(): CourseDto = CourseDto(
    code = code,
    name = name,
    institution = institution,
    ownerId = ownerId,
    studentCodes = studentCodes
)
