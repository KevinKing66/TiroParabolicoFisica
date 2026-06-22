package com.king.kevin.tiroparabolico.data.remote

import org.json.JSONObject
import javax.inject.Inject

class AuthResponseParser @Inject constructor() {
    fun extractToken(responseBody: String): String? {
        val trimmed = responseBody.trim()
        if (trimmed.isBlank()) return null
        if (trimmed.count { it == '.' } == 2) return trimmed

        return runCatching {
            val json = JSONObject(trimmed)
            json.optString("jsonwebtoken")
                .ifBlank { json.optString("jsonWebToken") }
                .ifBlank { json.optString("jwt") }
                .ifBlank { json.optString("token") }
                .ifBlank { json.optString("accessToken") }
                .takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
