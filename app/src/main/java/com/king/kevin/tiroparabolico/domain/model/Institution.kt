package com.king.kevin.tiroparabolico.domain.model

data class Institution(
    val id: String = "",
    val name: String,
    val address: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)
