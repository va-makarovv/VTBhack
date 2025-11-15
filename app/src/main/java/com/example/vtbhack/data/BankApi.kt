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
import kotlinx.serialization.SerialName

object BankApi {
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class AmountDto(
        @SerialName("amount")
        val amount: String,
        @SerialName("currency")
        val currency: String
    )
    @Serializable
    data class AccountDto(
        val accountId: String,
        val name: String? = null,
        val product: String? = null,
        val currency: String? = null,
        val balances: List<BalanceDto>? = null
    )


    @Serializable
    data class AccountsResponse(
        @SerialName("data")
        val data: AccountsData
    )

    @Serializable
    data class AccountsData(
        @SerialName("account")
        val account: List<AccountDto> = emptyList()
    )
    @Serializable
    data class TransactionDto(
        val transactionId: String,
        val bookingDateTime: String,
        val amount: AmountDto,
        val merchantName: String? = null,
        val description: String? = null
    )

    @Serializable
    data class TransactionsData(
        @SerialName("transaction")
        val transaction: List<TransactionDto> = emptyList()
    )
    @Serializable
    data class TransactionsResponse(
        @SerialName("data")
        val data: TransactionsData
    )

    @Serializable
    data class BalanceAmountDto(
        @SerialName("amount") val amount: String,
        @SerialName("currency") val currency: String
    )

    @Serializable
    data class BalanceDto(
        val accountId: String? = null,
        val amount: BalanceAmountDto,
        val creditDebitIndicator: String? = null
    )

    @Serializable
    data class BalancesData(
        @SerialName("balance") val balance: List<BalanceDto> = emptyList()
    )

    @Serializable
    data class BalancesResponse(
        @SerialName("data") val data: BalancesData
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

        // теперь data — объект, а список лежит в data.account
        return resp.data.account
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

        // аналогично: data -> transaction
        return resp.data.transaction
    }

    suspend fun getBalances(
        jwt: String,
        bank: String,
        accountId: String,
        clientId: String,
        consentId: String
    ): List<BalanceDto> {
        val resp: BalancesResponse =
            client.get("$BASE_URL/accounts/$accountId/balances") {
                header(HttpHeaders.Authorization, "Bearer $jwt")
                header("X-Consent-Id", consentId)
                parameter("bank", bank)
                parameter("client_id", clientId)
            }.body()

        return resp.data.balance
    }
}