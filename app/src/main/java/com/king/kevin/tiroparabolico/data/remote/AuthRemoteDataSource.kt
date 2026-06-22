package com.king.kevin.tiroparabolico.data.remote

import com.king.kevin.tiroparabolico.core.constants.PhysicsConstants
import com.king.kevin.tiroparabolico.data.dto.LoginRequestDto
import com.king.kevin.tiroparabolico.data.dto.RegisterRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor() {
    suspend fun login(request: LoginRequestDto): String = withContext(Dispatchers.IO) {
        postJson(
            endpoint = PhysicsConstants.LOGIN_ENDPOINT,
            body = JSONObject()
                .put("email", request.email)
                .put("password", request.password)
        )
    }

    suspend fun register(request: RegisterRequestDto): String = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("fullname", request.fullname)
            .put("password", request.password)
            .put("email", request.email)
            .put("nombreInstitucion", request.institutionName)

        request.courseCode
            ?.takeIf { it.isNotBlank() }
            ?.let { body.put("codigoCurso", it) }

        postJson(
            endpoint = PhysicsConstants.REGISTER_ENDPOINT,
            body = body
        )
    }

    private fun postJson(endpoint: String, body: JSONObject): String {
        val connection = (URL("${PhysicsConstants.AUTH_BASE_URL}$endpoint").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = TIMEOUT_MILLIS
            readTimeout = TIMEOUT_MILLIS
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body.toString())
            }

            val responseCode = connection.responseCode
            val responseBody = readResponse(connection, responseCode)
            if (responseCode !in 200..299) {
                throw IllegalStateException(extractErrorMessage(responseBody, responseCode))
            }
            responseBody
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(connection: HttpURLConnection, responseCode: Int): String {
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        return stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()
    }

    private fun extractErrorMessage(responseBody: String, responseCode: Int): String {
        val serverMessage = runCatching {
            val json = JSONObject(responseBody)
            json.optString("message")
                .ifBlank { json.optString("error") }
                .ifBlank { json.optString("detail") }
        }.getOrDefault("")

        return serverMessage.ifBlank { "Error de autenticacion. Codigo HTTP $responseCode." }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 15_000
    }
}
