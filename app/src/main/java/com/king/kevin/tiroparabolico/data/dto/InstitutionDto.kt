package com.king.kevin.tiroparabolico.data.dto

import com.king.kevin.tiroparabolico.domain.model.Institution

data class InstitutionDto(
    val name: String = "",
    val address: String = "",
    val createdAtMillis: Long = 0L
) {
    fun toDomain(id: String): Institution = Institution(
        id = id,
        name = name,
        address = address,
        createdAtMillis = createdAtMillis
    )
}

fun Institution.toDto(): InstitutionDto = InstitutionDto(
    name = name,
    address = address,
    createdAtMillis = createdAtMillis
)
