import com.example.vtbhack.AppSession
import com.example.vtbhack.data.BankApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BankRepository {

    var vbankConsentId: String? = null
    var sbankConsentId: String? = null
    var abankConsentId: String? = null
    private var accountsInternal: List<BankApi.AccountDto> = emptyList()
    private var accountsUi: List<BankAccountUi> = emptyList()
    var transactionsByAccount: Map<String, List<BankApi.TransactionDto>> = emptyMap()

    private const val CLIENT_LOGIN_IN_BANK = "team255-1"

    suspend fun refreshAllData() = withContext(Dispatchers.IO) {
        val jwt = AppSession.jwt ?: error("No JWT in session")

        val allAccounts = mutableListOf<BankApi.AccountDto>()
        val allAccountsUi = mutableListOf<BankAccountUi>()
        val txMap = mutableMapOf<String, List<BankApi.TransactionDto>>()

        // ВАЖНО: делаем suspend
        suspend fun loadBank(bankCode: String, consentId: String?) {
            // если для банка нет consentId – просто пропускаем его
            if (consentId.isNullOrBlank()) return

            // 1. Счета
            val accs = BankApi.getAccounts(
                jwt = jwt,
                bank = bankCode,
                clientId = CLIENT_LOGIN_IN_BANK,
                consentId = consentId
            )
            allAccounts += accs

            // 2. UI-модели + транзакции по каждому счёту
            for (dto in accs) {

                val last4 = dto.accountId.takeLast(4)
                val bankLabel = when (bankCode.lowercase()) {
                    "vbank" -> "VBank"
                    "abank" -> "ABank"
                    "sbank" -> "SBank"
                    else -> bankCode
                }

                val title = "$bankLabel **$last4"

                // баланс ты уже считаешь отдельно через getBalances()
                val balances = BankApi.getBalances(
                    jwt = jwt,
                    bank = bankCode,
                    accountId = dto.accountId,
                    clientId = CLIENT_LOGIN_IN_BANK,
                    consentId = consentId
                )
                val mainBalance = balances.firstOrNull()?.amount?.amount?.toDoubleOrNull() ?: 0.0

                allAccountsUi += BankAccountUi(
                    id = dto.accountId,
                    bank = bankCode,
                    title = title,
                    balance = mainBalance
                )

                val tx = BankApi.getTransactions(
                    jwt = jwt,
                    bank = bankCode,
                    accountId = dto.accountId,
                    clientId = CLIENT_LOGIN_IN_BANK,
                    consentId = consentId
                )
                txMap[dto.accountId] = tx
            }
        }

        // пробуем загрузить все банки, для которых уже есть consentId
        loadBank("vbank", vbankConsentId)
        loadBank("abank", abankConsentId)
        loadBank("sbank", sbankConsentId)

        accountsInternal = allAccounts
        accountsUi = allAccountsUi
        transactionsByAccount = txMap
    }

    data class BankAccountUi(
        val id: String,
        val bank: String,   // "vbank" / "abank" / "sbank"
        val title: String,  // текст на карточке
        val balance: Double // баланс
    )

    fun getAccountsUi(): List<BankAccountUi> = accountsUi

    fun getAccountsRaw(): List<BankApi.AccountDto> = accountsInternal

    fun getTransactions(accountId: String): List<BankApi.TransactionDto> =
        transactionsByAccount[accountId].orEmpty()
}