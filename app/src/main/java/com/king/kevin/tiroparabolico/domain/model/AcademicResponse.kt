package com.king.kevin.tiroparabolico.domain.model

data class AcademicResponse(
    val id: String = "",
    val userCode: String = "",
    val type: AcademicType,
    val labId: String = "general",
    val answers: Map<String, String>,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class AcademicType {
    ANALYSIS,
    CHALLENGES
}
