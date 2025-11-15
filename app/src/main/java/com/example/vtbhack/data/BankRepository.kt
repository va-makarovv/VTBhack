import com.example.vtbhack.AppSession
import com.example.vtbhack.data.BankApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BankRepository {



    private var accountsInternal: List<BankApi.AccountDto> = emptyList()
    private var accountsUi: List<BankAccountUi> = emptyList()
    var transactionsByAccount: Map<String, List<BankApi.TransactionDto>> = emptyMap()

    private const val BANK = "vbank"
    private const val CLIENT_LOGIN_IN_BANK = "team255-1"
    var consentId: String? = null

    suspend fun refreshAllData() = withContext(Dispatchers.IO) {
        val jwt = AppSession.jwt ?: error("No JWT in session")
        val consent = consentId ?: error("No consent id set in BankRepository")

        val accs = BankApi.getAccounts(jwt, BANK, CLIENT_LOGIN_IN_BANK, consent)
        accountsInternal = accs

        // готовим UI-модели
        accountsUi = accs.map { dto ->
            BankAccountUi(
                id = dto.accountId,
                bank = BANK, // пока один банк; когда пойдут мультибанки — сюда подставишь dto.bank
                title = dto.name ?: dto.product ?: "Счёт ${dto.accountId.takeLast(4)}",
                balance = dto.balances?.firstOrNull()?.amount ?: 0.0
            )
        }

        val txMap = mutableMapOf<String, List<BankApi.TransactionDto>>()
        for (acc in accs) {
            val tx = BankApi.getTransactions(
                jwt = jwt,
                bank = BANK,
                accountId = acc.accountId,
                clientId = CLIENT_LOGIN_IN_BANK,
                consentId = consent
            )
            txMap[acc.accountId] = tx
        }
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