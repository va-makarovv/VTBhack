package com.example.vtbhack.data


import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object ApiService {
    private const val BASE_URL = "http://localhost:5432"
    // если эмулятор и API крутится локально;
    // если используешь прод-сервер — подставь http://81.29.146.35:8080

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class LoginRequest(
        val phone_number: String,
        val password: String
    )

    @Serializable
    data class LoginResponse(
        val token: String
    )

    suspend fun login(phone: String, password: String): String {
        val resp: LoginResponse = client.post("$BASE_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(phone_number = phone, password = password))
        }.body()
        return resp.token
    }
}