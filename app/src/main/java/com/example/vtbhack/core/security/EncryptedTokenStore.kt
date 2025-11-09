package com.example.vtbhack.core.security

interface EncryptedTokenStore {
    suspend fun save(access: String, refresh: String?)
    suspend fun access(): String?
    suspend fun refresh(): String?
    suspend fun clear()
}