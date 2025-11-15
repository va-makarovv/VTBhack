package com.example.vtbhack



import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppSession {
    // Телефон, который ввёл пользователь (в формате +7...)
    var phone by mutableStateOf<String?>(null)

    // JWT-токен, который ты получил с /login
    var jwt by mutableStateOf<String?>(null)
}