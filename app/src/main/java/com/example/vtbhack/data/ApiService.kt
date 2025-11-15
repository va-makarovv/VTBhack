package com.example.vtbhack.data

import android.util.Log
import com.example.vtbhack.AppSession
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

@Serializable
data class ConsentRequest(
    val client_id: String,
    val permissions: List<String>,
    val reason: String = "Show accounts in our app",
    val requesting_bank: String = "team255",
    val requesting_bank_name: String = "Team 255 App"
)

@Serializable
data class ConsentResponse(
    val request_id: String? = null,
    val consent_id: String? = null,
    val status: String? = null,
    val saved: Boolean? = null,
    val http_status: Int? = null
)

data class BankConsents(
    val vbank: String?,
    val sbank: String?,
    val abank: String?
)

@Serializable
data class AccountsResponse(
    val accounts: List<AccountDto>
)

@Serializable
data class AccountDto(
    val id: String,
    val name: String,
    val balance: Double,
    val currency: String
    // сделай под реальный ответ твоего бэка, можешь адаптировать к openbank
)

@Serializable
data class TransactionsResponse(
    val transactions: List<TransactionDto>
)

@Serializable
data class TransactionDto(
    val id: String,
    val accountId: String,
    val amount: Double,
    val currency: String,
    val merchantName: String? = null,
    val bookingDateTime: String,
    val creditDebitIndicator: String
)

object ApiService {

    // проверь, что URL совпадает с твоим бэком
    private const val BASE_URL = "http://10.0.2.2:8080"
    // или "http://81.29.146.35:8080" на внешний стенд
    private const val CLIENT_LOGIN = "team255-1"
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

    private fun HttpRequestBuilder.authHeader() {
        val token = AppSession.jwt
        if (!token.isNullOrBlank()) {
            header(HttpHeaders.Authorization, "Bearer $token")
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

    suspend fun createAccountsConsent(bank: String): ConsentResponse {
        val response = client.post("$BASE_URL/consents/request") {
            authHeader()
            parameter("bank", bank)         // vbank / sbank / abank
            contentType(ContentType.Application.Json)
            setBody(
                ConsentRequest(
                    client_id = CLIENT_LOGIN,
                    permissions = listOf(
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadTransactionsDetail"
                    )
                )
            )
        }

        val raw = response.bodyAsText()
        Log.d("CONSENT_POST", "bank=$bank, status=${response.status.value}, body=$raw")

        if (!response.status.isSuccess()) {
            throw Exception("Ошибка создания consent ($bank): ${response.status.value}: $raw")
        }

        return response.body()
    }

    suspend fun getConsentByRequestId(bank: String, requestId: String): ConsentResponse {
        val response = client.get("$BASE_URL/consents/$requestId") {
            authHeader()
            parameter("bank", bank)
        }

        val raw = response.bodyAsText()
        Log.d("CONSENT_GET", "bank=$bank, status=${response.status.value}, body=$raw")

        if (!response.status.isSuccess()) {
            throw Exception("Ошибка получения consent ($bank): ${response.status.value}: $raw")
        }

        return response.body()
    }

    suspend fun getConsentsForAllBanks(): BankConsents {
        var vbankConsent: String? = null
        var sbankConsent: String? = null
        var abankConsent: String? = null

        // 1) VBank
        run {
            val resp = createAccountsConsent("vbank")
            vbankConsent = resp.consent_id
        }

        // 2) ABank
        run {
            val resp = createAccountsConsent("abank")
            abankConsent = resp.consent_id
        }

        // 3) SBank (сложный: сначала request_id)
        run {
            val resp = createAccountsConsent("sbank")
            val cid = resp.consent_id
            val rid = resp.request_id

            sbankConsent = when {
                cid != null -> cid                        // банк сразу выдал consent_id
                rid != null -> {
                    // UX-часть: ты показываешь пользователю текст:
                    // “Подтвердите согласие в SBank и нажмите кнопку”
                    //
                    // В простой версии на демо можно
                    //   – сделать отдельную кнопку "Я подтвердил в SBank"
                    //   – а здесь только вернуть request_id и где-то сохранить.
                    null
                }
                else -> null
            }
        }

        return BankConsents(
            vbank = vbankConsent,
            sbank = sbankConsent,
            abank = abankConsent
        )
    }

    suspend fun getAccounts(
        bank: String,
        consentId: String
    ): AccountsResponse {
        val response = client.get("$BASE_URL/accounts") {
            authHeader()
            parameter("bank", bank)
            parameter("client_id", CLIENT_LOGIN)
            header("X-Consent-Id", consentId)
        }

        val raw = response.bodyAsText()
        Log.d("ACCOUNTS", "bank=$bank, status=${response.status.value}, body=$raw")

        if (!response.status.isSuccess()) {
            throw Exception("Ошибка счетов ($bank): ${response.status.value}: $raw")
        }

        return response.body()
    }

    suspend fun getTransactions(
        bank: String,
        accountId: String,
        consentId: String
    ): TransactionsResponse {
        val response = client.get("$BASE_URL/accounts/$accountId/transactions") {
            authHeader()
            parameter("bank", bank)
            header("X-Consent-Id", consentId)
        }

        val raw = response.bodyAsText()
        Log.d("TXNS", "bank=$bank, status=${response.status.value}, body=$raw")

        if (!response.status.isSuccess()) {
            throw Exception("Ошибка транзакций ($bank): ${response.status.value}: $raw")
        }

        return response.body()
    }
}

