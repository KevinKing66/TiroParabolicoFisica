package com.king.kevin.tiroparabolico.data.remote

import android.util.Base64
import com.king.kevin.tiroparabolico.domain.model.UserSession
import org.json.JSONObject

class JwtParser {
    fun parse(token: String): UserSession {
        val payload = token.split(".").getOrNull(1)
            ?: throw IllegalArgumentException("La respuesta no contiene un JWT valido.")
        val decoded = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP), Charsets.UTF_8)
        val json = JSONObject(decoded)

        return UserSession(
            token = token,
            fullName = json.findValue("fullname", "fullName", "name", "nombreCompleto"),
            code = json.findValue("code", "codigo", "userCode"),
            role = json.findValue("rol", "role"),
            course = json.findValue("curso", "course", "courseCode", "codigoCurso")
        )
    }

    private fun JSONObject.findValue(vararg keys: String): String {
        return keys.firstNotNullOfOrNull { key -> optString(key).takeIf { it.isNotBlank() } }.orEmpty()
    }
}
