package com.example.vtbhack.data

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LoginRequest(
    val phone_number: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val phone_number: String,
    val password: String,
    val role: String = "user"
)

@Serializable
data class LoginResponse(
    val token: String
)

object ApiService {

    // проверь, что URL совпадает с твоим бэком
    private const val BASE_URL = "http://10.0.2.2:8080"
    // или "http://81.29.146.35:8080" если ходишь на внешний стенд

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                }
            )
        }
    }

    suspend fun login(phone: String, password: String): String {
        // 1. Пытаемся залогиниться
        val loginResponse = client.post("$BASE_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(phone_number = phone, password = password))
        }

        val loginRaw = loginResponse.bodyAsText()
        Log.d("LOGIN", "status=${loginResponse.status.value}, body=$loginRaw")

        return when (loginResponse.status.value) {
            in 200..299 -> {
                // успех — просто возвращаем токен
                val body: LoginResponse = loginResponse.body()
                body.token
            }

            404 -> {
                // user_not_found -> создаём пользователя через /register
                val registerResponse = client.post("$BASE_URL/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        RegisterRequest(
                            name = phone,          // можно заменить на имя, если добавите экран
                            phone_number = phone,
                            password = password,
                            role = "user"
                        )
                    )
                }

                val registerRaw = registerResponse.bodyAsText()
                Log.d("REGISTER", "status=${registerResponse.status.value}, body=$registerRaw")

                if (registerResponse.status.isSuccess()) {
                    // /register возвращает { "token": "...", "user": {...} }
                    // нам достаточно забрать token той же LoginResponse-моделью
                    val body: LoginResponse = registerResponse.body()
                    body.token
                } else {
                    throw Exception("Регистрация не удалась: ${registerResponse.status.value}: $registerRaw")
                }
            }

            401 -> {
                // invalid_credentials
                throw Exception("Неверный пароль")
            }

            else -> {
                throw Exception("Ошибка входа: ${loginResponse.status.value}: $loginRaw")
            }
        }
    }
}