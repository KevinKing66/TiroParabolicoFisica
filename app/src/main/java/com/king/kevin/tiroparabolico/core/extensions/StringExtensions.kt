package com.king.kevin.tiroparabolico.core.extensions

fun String.toNullableDouble(): Double? = replace(',', '.').trim().toDoubleOrNull()
