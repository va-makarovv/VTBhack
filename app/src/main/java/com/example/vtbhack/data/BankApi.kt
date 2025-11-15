package com.example.vtbhack.data

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object BankApi {
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class AccountDto(
        val accountId: String,
        val name: String? = null,
        val product: String? = null,
        val currency: String? = null,
        val balances: List<BalanceDto>? = null
    )

    @Serializable
    data class BalanceDto(
        val amount: Double,
        val currency: String
    )

    @Serializable
    data class AccountsResponse(
        val data: List<AccountDto>
    )

    @Serializable
    data class TransactionDto(
        val transactionId: String,
        val bookingDateTime: String,
        val amount: Double,
        val currency: String,
        val merchantName: String? = null,
        val description: String? = null
    )

    @Serializable
    data class TransactionsResponse(
        val data: List<TransactionDto>
    )

    suspend fun getAccounts(
        jwt: String,
        bank: String,
        clientId: String,
        consentId: String
    ): List<AccountDto> {
        val resp: AccountsResponse = client.get("$BASE_URL/accounts") {
            header("Authorization", "Bearer $jwt")
            header("X-Consent-Id", consentId)
            parameter("bank", bank)
            parameter("client_id", clientId)
        }.body()
        return resp.data
    }

    suspend fun getTransactions(
        jwt: String,
        bank: String,
        accountId: String,
        clientId: String,
        consentId: String
    ): List<TransactionDto> {
        val resp: TransactionsResponse =
            client.get("$BASE_URL/accounts/$accountId/transactions") {
                header("Authorization", "Bearer $jwt")
                header("X-Consent-Id", consentId)
                parameter("bank", bank)
                parameter("client_id", clientId)
            }.body()
        return resp.data
    }
}