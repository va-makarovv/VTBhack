package com.example.vtbhack.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class EncryptedTokenStoreImpl(context: Context) : EncryptedTokenStore  {


    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private companion object {
        const val K_ACCESS = "access_token"
        const val K_REFRESH = "refresh_token"
    }


    override suspend fun save(access: String, refresh: String?) = withContext(Dispatchers.IO) {
        prefs.edit().putString(K_ACCESS, access).apply()
        if (refresh != null) prefs.edit().putString(K_REFRESH, refresh).apply()
    }

    override suspend fun access(): String? = withContext(Dispatchers.IO) {
        prefs.getString(K_ACCESS, null)
    }

    override suspend fun refresh(): String? = withContext(Dispatchers.IO) {
        prefs.getString(K_REFRESH, null)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().remove(K_ACCESS).remove(K_REFRESH).apply()
    }
}