package com.example.vtbhack

import androidx.annotation.DrawableRes

import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.vtbhack.ui.theme.VTBhackTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.vtbhack.data.ApiService
import kotlinx.coroutines.launch
import com.example.vtbhack.toRub

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VTBhackTheme {
                AppNavHost()
            }
        }
    }
}

private val AppBg = Color(0xFF141414)

private object Routes {
    const val AUTH = "auth"
    const val APP = "app"

    object Auth {
        const val PHONE = "auth/phone"
        const val PASSWORD = "auth/password/{phone}"
        const val WELCOME = "auth/welcome"
    }

    object AppInner {
        const val HOME = "app/home"
        const val PAYMENTS = "app/payments"
        const val ANALYTICS = "app/analytics"
        const val MORE = "app/more"
        const val FINANCE_DISTRIBUTION = "app/finance_distribution"
        const val CARDS_ANALYTICS = "app/cards_analytics"
        const val CARD_DETAILS = "app/card_details/{accountId}"
        const val CARD_CHALLENGES = "app/card_challenges/{accountId}"
        const val SBANK_CONSENT = "app/sbank_consent"

    }
}



@Composable
private fun AppNavHost() {
    val navController = rememberNavController()

    // TODO: Replace with your real EncryptedTokenStore check
    // e.g. val isAuthorized = remember { EncryptedTokenStore(LocalContext.current).hasToken() }
    val isAuthorized by remember { mutableStateOf(false) }

    val start = if (isAuthorized) Routes.APP else Routes.AUTH

    NavHost(
        navController = navController,
        startDestination = start
    ) {
        // AUTH GRAPH
        navigation(
            startDestination = Routes.Auth.PHONE,
            route = Routes.AUTH
        ) {
            composable(Routes.Auth.PHONE) {
                PhoneEntryScreen(
                    onContinue = { phoneE164 ->
                        // phoneE164 already contains +7...
                        navController.navigate(Routes.Auth.PASSWORD.replace("{phone}", phoneE164))
                    }
                )
            }

            composable(Routes.Auth.PASSWORD) { backStackEntry ->
                val phone = backStackEntry.arguments?.getString("phone").orEmpty()
                PasswordEntryScreen(
                    phone = phone,
                    onSignIn = { token ->
                        // Save phone used for auth to show it in ProfileScreen
                        AppSession.phone = phone
                        AppSession.jwt = token
                        navController.navigate(Routes.Auth.WELCOME)

                        // потом унесём jwt в ESP

                        // Go to welcome screen
                        navController.navigate(Routes.Auth.WELCOME) {
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.Auth.WELCOME) {
                WelcomeScreen(
                    onContinue = {
                        // Enter the main app and CLEAR auth from back stack
                        navController.navigate(Routes.APP) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }


        }

        // APP GRAPH (BottomBar)
        navigation(
            startDestination = Routes.AppInner.HOME,
            route = Routes.APP
        ) {
            composable(Routes.AppInner.HOME) {
                AppShell(initial = Routes.AppInner.HOME)
            }
            composable(Routes.AppInner.PAYMENTS) {
                AppShell(initial = Routes.AppInner.PAYMENTS)
            }
            composable(Routes.AppInner.ANALYTICS) {
                AppShell(initial = Routes.AppInner.ANALYTICS)
            }
            composable(Routes.AppInner.MORE) {
                AppShell(initial = Routes.AppInner.MORE)
            }
        }
    }
}

@Composable
private fun AppShell(initial: String) {
    val innerNav = rememberNavController()

    // Inner host for bottom tabs
    Scaffold(
        containerColor = AppBg,
        bottomBar = {
            BottomBar(
                currentRoute = currentRoute(innerNav),
                onNavigate = { route ->
                    innerNav.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(innerNav.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = initial,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.AppInner.HOME) {
                HomeScreen(
                    onOpenPayments = { innerNav.navigate(Routes.AppInner.PAYMENTS) },
                    onOpenFinance = { innerNav.navigate(Routes.AppInner.FINANCE_DISTRIBUTION) },
                    onOpenCardAnalytics = { innerNav.navigate(Routes.AppInner.CARDS_ANALYTICS) },
                    onOpenCardDetails = { accountId ->
                        innerNav.navigate("app/card_details/$accountId")}
                )
            }
            composable("app/card_details/{accountId}") { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
                CardDetailsScreen(
                    accountId = accountId,
                    onBack = { innerNav.popBackStack() },
                    onOpenSettings = {
                        innerNav.navigate("app/card_challenges/$accountId")
                    }
                )
            }
            composable("app/card_challenges/{accountId}") { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
                CardChallengesScreen(
                    accountId = accountId,
                    onBack = { innerNav.popBackStack() }
                )
            }

            composable(Routes.AppInner.PAYMENTS) { PaymentsScreen() }
            composable(Routes.AppInner.ANALYTICS) { AnalyticsScreen() }
            composable(Routes.AppInner.MORE) {
                ProfileScreen(
                    onOpenSBankConsent = { innerNav.navigate(Routes.AppInner.SBANK_CONSENT) }
                )
            }
            composable(Routes.AppInner.FINANCE_DISTRIBUTION) { FinanceDistributionScreen() }
            composable(Routes.AppInner.CARDS_ANALYTICS) { CardsAnalyticsScreen() }

            composable(Routes.AppInner.SBANK_CONSENT) {
                SBankConsentScreen(
                    onBack = { innerNav.popBackStack() },
                    onConsentReady = { consentId ->
                        // тут можешь сохранить в какой-нибудь репо
                        BankRepository.sbankConsentId = consentId
                        innerNav.popBackStack()
                    }
                )
            }

        }
    }
}

private data class TabItem(val route: String, val label: String, @DrawableRes val iconRes: Int)

@Composable
private fun BottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(
        TabItem(Routes.AppInner.HOME, "Главная", R.drawable.ic_home),
        TabItem(Routes.AppInner.PAYMENTS, "Оплата", R.drawable.ic_payments),
        TabItem(Routes.AppInner.ANALYTICS, "Анализ", R.drawable.ic_analytics),
        TabItem(Routes.AppInner.MORE, "Профиль", R.drawable.ic_more)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp, bottom = 30.dp)
    ) {
        // Background plate from design (353x87). Put drawable as res/drawable/frame_2.png
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(87.dp)
                .background(Color(0xFF272727), shape = RoundedCornerShape(20.dp))
        )

        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onNavigate(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label,

                        alpha = if (selected) 1f else 0.6f
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        color = if (selected) Color.White else Color(0xFFB0B0B0)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onOpenPayments: () -> Unit = {},
    onOpenFinance: () -> Unit = {},
    onOpenCardAnalytics: () -> Unit = {},
    onOpenCardDetails: (String) -> Unit
) {
    val background = AppBg
    val hint = Color(0xFFB0B0B0)

    var accounts by remember { mutableStateOf<List<BankRepository.BankAccountUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // 1. Если ещё не запрашивали consents для vbank/abank – делаем это
            if (BankRepository.vbankConsentId == null || BankRepository.abankConsentId == null) {
                val consents = ApiService.getConsentsForAllBanks()

                BankRepository.vbankConsentId = consents.vbank
                    ?: error("VBank не вернул consent_id")

                // ABank может быть опциональным – если не критично, можно не падать
                BankRepository.abankConsentId = consents.abank
            }

            // 2. Грузим все доступные банки (vbank + abank, а позже и sbank)
            BankRepository.refreshAllData()
            accounts = BankRepository.getAccountsUi()
        } catch (e: Exception) {
            errorText = "Ошибка загрузки счетов: ${e.message}"
        } finally {
            isLoading = false
        }
    }


    Surface(color = background) {
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(horizontal = 30.dp)
                    .padding(top = 24.dp, bottom = 120.dp) // bottom space so part can scroll under BottomBar
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color(0xFF2B2B2B)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Профиль", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                    Icon(imageVector = Icons.Filled.Apps, contentDescription = "Уведомления", tint = Color.White.copy(0.8f))
                }


                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Общий баланс",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))

                val total = accounts.sumOf { it.balance }

                Text(
                    text = total.toRub(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                // Cards row
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = Color(0xFFFF4D4F),
                        fontSize = 13.sp
                    )
                }

                if (isLoading) {
                    Text(
                        text = "Загружаем карты...",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(accounts) { acc ->
                            val cardImageRes = when (acc.bank.lowercase()) {
                                "vbank" -> R.drawable.vbank_card
                                "abank" -> R.drawable.abank_card
                                "sbank" -> R.drawable.sbank_card
                                else -> R.drawable.card_blue
                            }

                            CardTile(
                                bankName = acc.title,
                                amount = acc.balance.toRub(),
                                imageRes = cardImageRes,
                                modifier = Modifier.width(180.dp),
                                onClick = { onOpenCardDetails(acc.id) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Quick actions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1C1C1E)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionItem(icon = Icons.Filled.Payment, label = "Платить", onClick = onOpenPayments)
                        ActionItem(icon = Icons.Filled.CreditCard, label = "Положить деньги")
                        ActionItem(icon = Icons.Filled.Apps, label = "Перевести")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TransactionsTile(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenPayments() }
                    )
                    CashbackTile(
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                WideTile(
                    title = "Распредели финансы",
                    icon = Icons.Filled.Apps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFinance() }
                )

                Spacer(Modifier.height(12.dp))

                WideTile(
                    title = "Аналитика по картам",
                    icon = Icons.Filled.BarChart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCardAnalytics() }
                )
            }
        }
    }
}

@Composable
private fun CardTile(
    bankName: String,
    amount: String,
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier,
    variant: Int = 0,
    onClick: () -> Unit = {}
) {
    val bg = if (variant == 0) {
        Brush.verticalGradient(listOf(Color(0xFF202020), Color(0xFF272727)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF26213A), Color(0xFF2F2A40)))
    }
    Box(
        modifier = modifier
            .height(175.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 130.dp, height = 83.dp)
                    .align(Alignment.TopStart),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(text = bankName, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(text = amount, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ActionItem(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = Color(0xFF272727)) {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Color.White)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(text = label, color = Color.White, fontSize = 12.sp, lineHeight = 12.sp)
    }
}

@Composable
private fun TransactionsTile(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(140.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF1C1C1E)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Транзакции", color = Color.White.copy(0.9f), fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text("33 333 ₽", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("потрачено в Июне", color = Color(0xFF90EE90), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                trackColor = Color(0xFF2A2A2A)
            )
            Spacer(Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                trackColor = Color(0xFF2A2A2A)
            )
        }
    }
}

@Composable
private fun CashbackTile(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(140.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF1C1C1E)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Surface(shape = CircleShape, color = Color(0xFF2B2B2B)) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Text("360 ₽", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Заработано кэшбэка", color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text("Отправить на счет", color = Color(0xFFB0B0B0), fontSize = 12.sp)
        }
    }
}

@Composable
private fun WideTile(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(72.dp), shape = RoundedCornerShape(16.dp), color = Color(0xFF1C1C1E)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Icon(icon, contentDescription = null, tint = Color.White.copy(0.8f))
        }
    }
}
@Composable
private fun PaymentsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Оплата") }
}
@Composable
private fun AnalyticsScreen() {
    val background = AppBg
    val tileBg = Color(0xFF1C1C1E)
    val hint = Color(0x99FFFFFF)

    Surface(color = background) {
        val scroll = rememberScrollState()
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(horizontal = 30.dp)
                    .padding(top = 12.dp, bottom = 120.dp)
            ) {
                // Top row (only settings on the right to match bottom-tab context)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(24.dp))
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Настройки",
                        tint = Color.White.copy(0.9f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Cards carousel
                AnalyticsCardsRow()

                Spacer(Modifier.height(16.dp))

                // Траты/Доход
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = tileBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        Text(
                            "Траты/Доход",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(12.dp))

                        val max = 400_000f
                        StatRow(
                            label = "Доход",
                            amountText = "400 000 ₽",
                            progress = 400_000f / max,
                            barColor = Color(0xFF9B7CFF),
                            hint = hint
                        )
                        Spacer(Modifier.height(8.dp))
                        StatRow(
                            label = "Траты",
                            amountText = "230 000 ₽",
                            progress = 230_000f / max,
                            barColor = Color(0xFFFF91A4),
                            hint = hint
                        )
                        Spacer(Modifier.height(8.dp))
                        StatRow(
                            label = "Различие",
                            amountText = "170 000 ₽",
                            progress = 170_000f / max,
                            barColor = Color(0xFFC3F0E7),
                            hint = hint
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Траты по категориям
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = tileBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Траты по категориям",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF2B2B2B)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ноябрь", color = Color.White, fontSize = 14.sp)
                                    Icon(
                                        imageVector = Icons.Filled.ExpandMore,
                                        contentDescription = null,
                                        tint = Color.White.copy(0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text("9 922 ₽", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val segments = listOf(
                                Segment(0.50f, Color(0xFFFFD64D)), // жёлтый
                                Segment(0.30f, Color(0xFF9B7CFF)), // фиолетовый
                                Segment(0.15f, Color(0xFF8FE5E2)), // бирюзовый
                                Segment(0.05f, Color(0xFFFF7A7A))  // красный
                            )
                            DonutChart(segments = segments, thickness = 26.dp, gapDegrees = 2f, modifier = Modifier.padding(16.dp))
                        }

                        Spacer(Modifier.height(8.dp))

                        var period by remember { mutableStateOf(1) } // 0-неделя, 1-месяц, 2-год
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PeriodChip("Неделя", selected = period == 0) { period = 0 }
                            PeriodChip("Месяц", selected = period == 1) { period = 1 }
                            PeriodChip("Год", selected = period == 2) { period = 2 }
                        }
                    }
                }
            }
        }
    }
}

private data class Segment(val fraction: Float, val color: Color)

@Composable
private fun AnalyticsCardsRow() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val cards = listOf(
            "Alfa-Bank **2345" to "115 000 ₽",
            "T-Bank **1890" to "5000 ₽",
            "T-Bank **1890" to "5000 ₽"
        )
        items(cards) { (title, amount) ->
            CardSmall(title = title, amount = amount)
        }
    }
}

@Composable
private fun CardSmall(title: String, amount: String) {
    val bg = Brush.verticalGradient(listOf(Color(0xFF202020), Color(0xFF272727)))
    Column(
        modifier = Modifier
            .width(160.dp)
    ) {
        Box(
            modifier = Modifier
                .height(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
        ) {
            Image(
                painter = painterResource(id = R.drawable.card_blue),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 130.dp, height = 83.dp)
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                contentScale = ContentScale.FillBounds
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(text = title, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(text = amount, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatRow(
    label: String,
    amountText: String,
    progress: Float,
    barColor: Color,
    hint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
        Text(amountText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(6.dp))
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        color = barColor,
        trackColor = Color(0xFF2A2A2A)
    )
}

@Composable
private fun PeriodChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bgSelected = Color(0xFF2B2B2B)
    val bg = if (selected) bgSelected else bgSelected.copy(alpha = 0.5f)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun DonutChart(
    segments: List<Segment>,
    thickness: Dp,
    gapDegrees: Float = 0f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val diameter = size.minDimension
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val stroke = Stroke(width = thickness.toPx(), cap = StrokeCap.Butt)
        var start = -90f
        segments.forEach { seg ->
            val sweep = 360f * seg.fraction - gapDegrees
            if (sweep > 0f) {
                drawArc(
                    color = seg.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                start += 360f * seg.fraction
            }
        }
    }
}
@Composable
private fun MoreScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Основное") }
}
@Composable
private fun ProfileScreen(
    onOpenSBankConsent: () -> Unit = {}
) {
    val background = AppBg
    val fieldBg = Color(0xFF1C1C1E)
    val hint = Color(0x99FFFFFF)
    val pillPurple = Color(0xFF8B7CF6)

    val phoneRaw = AppSession.phone ?: "+7"
    val phoneUi = remember(phoneRaw) { formatE164ForProfile(phoneRaw) }

    Surface(color = background) {
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(horizontal = 30.dp)
                    .padding(top = 12.dp, bottom = 120.dp)
            ) {
                // Top bar with centered title and actions on the right
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Профиль",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Apps, // bell substitute
                            contentDescription = "Уведомления",
                            tint = Color.White.copy(0.9f)
                        )
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Настройки",
                            tint = Color.White.copy(0.9f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Subtle header gradient under the top bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x3340E0D0), // soft glow
                                    Color.Transparent
                                )
                            )
                        )
                )

                Spacer(Modifier.height(16.dp))

                // Avatar
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(112.dp),
                        shape = CircleShape,
                        color = Color(0xFF2B2B2B)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Phone number
                Text(
                    text = phoneUi,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(24.dp))

                // Subscription level pill
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = pillPurple
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Уровень подписки",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White
                        ) {
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 64.dp)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pro",
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Support tile
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = fieldBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Поддержка",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2B2B2B)
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                val isSBankConnected = BankRepository.sbankConsentId != null
                val sBankTitle = if (isSBankConnected) {
                    "SBank уже подключён"
                } else {
                    "Подключить SBank"
                }

                WideTile(
                    title = sBankTitle,
                    icon = Icons.Filled.OpenInNew,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isSBankConnected) {
                                onOpenSBankConsent()
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun FinanceDistributionScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Распредели финансы") }
}

@Composable
private fun CardsAnalyticsScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Аналитика по картам") }
}
@Composable
private fun CardDetailsScreen(
    accountId: String,
    onBack: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val background = AppBg
    val hint = Color(0xFFB0B0B0)

    // Находим нужный счёт и его транзакции
    val accountUi = remember(accountId) {
        BankRepository.getAccountsUi().firstOrNull { it.id == accountId }
    }
    val transactions = remember(accountId) {
        BankRepository.getTransactions(accountId)
    }

    // Локальное состояние категорий (для реактивного UI) + синк с репозиторием
    val localCategories = remember(accountId) {
        mutableStateMapOf<String, String>().apply {
            transactions.forEach { tx ->
                BankRepository.getTransactionCategory(tx.transactionId)?.let { put(tx.transactionId, it) }
            }
        }
    }

    // Какая транзакция сейчас редактируется в диалоге выбора категории
    var categoryDialogTxId by remember { mutableStateOf<String?>(null) }

    fun parseAmount(str: String?): Double {
        if (str == null) return 0.0
        return str.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    // Считаем суммы и признак доход/расход
    val txWithAmount = remember(accountId, transactions) {
        transactions.map { tx ->
            val amount = parseAmount(tx.amount.amount)
            Triple(tx, amount, amount >= 0.0) // third = isIncome (если знак другой, поменяешь логически)
        }
    }

    val totalIncome = txWithAmount.filter { it.third }.sumOf { it.second }
    val totalExpense = txWithAmount.filter { !it.third }.sumOf { kotlin.math.abs(it.second) }

    Surface(color = background) {
        Box(Modifier.fillMaxSize()) {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp, end = 30.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки", tint = Color.White.copy(0.9f))
                    }
                }

                // Card header with subtle gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2C2242), Color.Transparent),
                                tileMode = TileMode.Clamp
                            )
                        )
                        .padding(top = 20.dp, bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.card_blue),
                            contentDescription = null,
                            modifier = Modifier.size(width = 188.dp, height = 120.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            accountUi?.title ?: "Счёт $accountId",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        val balanceText = accountUi?.balance?.toRub() ?: "--"
                        Text(
                            balanceText,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Показываем первые две карты из репозитория
                            val allAccounts = BankRepository.getAccountsUi()
                            allAccounts.take(2).forEach { acc ->
                                MiniCard(
                                    title = acc.title,
                                    amount = acc.balance.toRub()
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Итого по счёту",
                    color = hint,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiChip(
                        title = "Доходы",
                        value = totalIncome.toRub(),
                        positive = true,
                        modifier = Modifier.weight(1f)
                    )
                    KpiChip(
                        title = "Расходы",
                        value = totalExpense.toRub(),
                        positive = false,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Группировка транзакций по дате
                val grouped = txWithAmount
                    .groupBy { (tx, _, _) -> formatTransactionDayLabel(tx.bookingDateTime) }
                    .toSortedMap(compareByDescending { it })

                grouped.forEach { (dayLabel, list) ->
                    val dayExpenses = list
                        .filter { !it.third }
                        .sumOf { kotlin.math.abs(it.second) }

                    val totalText = if (dayExpenses > 0.0) {
                        "Итого: -${dayExpenses.toRub()}"
                    } else {
                        ""
                    }

                    DayHeader(day = dayLabel, total = totalText)

                    list.forEach { (tx, amount, isIncome) ->
                        val txId = tx.transactionId
                        val amountAbs = kotlin.math.abs(amount)
                        val amountText = (if (isIncome) "+ " else "- ") + amountAbs.toRub()
                        val category = localCategories[txId]
                            ?: BankRepository.getTransactionCategory(txId)

                        TransactionRow(
                            isIncome = isIncome,
                            title = category ?: "Без категории",
                            bank = accountUi?.title ?: "Счёт $accountId",
                            merchant = tx.merchantName ?: (tx.description ?: ""),
                            amountText = amountText,
                            category = category,
                            onCategoryClick = {
                                categoryDialogTxId = txId
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(120.dp)) // bottom space to scroll above bottom bar
            }

            // Floating edit button (пока можно оставить заглушкой)
            FloatingActionButton(
                onClick = { /* edit */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(30.dp),
                containerColor = Color(0xFF2F80ED)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
        }

        // Диалог выбора категории
        if (categoryDialogTxId != null) {
            val categories = listOf("Еда", "Транспорт", "Подписки", "Развлечения", "Другое")
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { categoryDialogTxId = null },
                confirmButton = {},
                title = { Text("Категория транзакции") },
                text = {
                    Column {
                        categories.forEach { cat ->
                            TextButton(
                                onClick = {
                                    val txId = categoryDialogTxId ?: return@TextButton
                                    localCategories[txId] = cat
                                    BankRepository.setTransactionCategory(txId, cat)
                                    categoryDialogTxId = null
                                }
                            ) {
                                Text(cat)
                            }
                        }
                        TextButton(
                            onClick = {
                                val txId = categoryDialogTxId ?: return@TextButton
                                localCategories.remove(txId)
                                BankRepository.setTransactionCategory(txId, "Без категории")
                                categoryDialogTxId = null
                            }
                        ) {
                            Text("Без категории")
                        }
                    }
                }
            )
        }
    }
}
@Composable
private fun MiniCard(title: String, amount: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Image(
            painter = painterResource(id = R.drawable.card_blue),
            contentDescription = null,
            modifier = Modifier.size(width = 106.dp, height = 68.dp),
            contentScale = ContentScale.FillBounds
        )
        Spacer(Modifier.height(6.dp))
        Text(text = title, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(text = amount, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun KpiChip(title: String, value: String, positive: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 64.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ColoredDot(color = if (positive) Color(0xFF00C853) else Color(0xFFFF5252))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.Black.copy(alpha = 0.8f), fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
                Text(value, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = Color.Black.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun DayHeader(day: String, total: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(day, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        if (total.isNotBlank()) {
            Text(total, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }
    }
}
@Composable
private fun TransactionRow(
    isIncome: Boolean,
    title: String,
    bank: String,
    merchant: String,
    amountText: String,
    category: String?,
    onCategoryClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = Color(0xFF2B2B2B)) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                if (isIncome) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF00C853))
                } else {
                    Icon(Icons.Filled.Remove, contentDescription = null, tint = Color.White)
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColoredDot(color = if (isIncome) Color(0xFF00C853) else Color(0xFFFF5252))
                Spacer(Modifier.width(6.dp))
                Text(
                    title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(bank, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Text(merchant, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val catText = category ?: "Без категории"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2B2B2B),
                    modifier = Modifier
                        .clickable(enabled = onCategoryClick != null) {
                            onCategoryClick?.invoke()
                        }
                ) {
                    Text(
                        text = "Категория: $catText",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = amountText,
            color = if (isIncome) Color(0xFF00C853) else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
@Composable
private fun ColoredDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private fun formatTransactionDayLabel(raw: String): String {
    // Ожидаемый формат из банка обычно "YYYY-MM-DD..." – берём первые 10 символов
    val datePart = raw.take(10) // например, "2024-11-05"
    return try {
        val parts = datePart.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: return datePart
            val day = parts[2]
            val monthNames = arrayOf(
                "января",
                "февраля",
                "марта",
                "апреля",
                "мая",
                "июня",
                "июля",
                "августа",
                "сентября",
                "октября",
                "ноября",
                "декабря"
            )
            val monthName = monthNames.getOrNull(month - 1) ?: return datePart
            "$day $monthName"
        } else {
            datePart
        }
    } catch (e: Exception) {
        datePart
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val backStackEntry by navController.currentBackStackEntryAsState()
    return backStackEntry?.destination?.route
}
@Composable
fun PhoneEntryScreen(
    onContinue: (String) -> Unit = {}
) {
    val background = AppBg
    val fieldBg = Color(0xFF1C1C1E)
    val hint = Color(0x99FFFFFF)
    val buttonPurple = Color(0xFF8B7CF6) // мягкий фиолетовый
    var digits by remember { mutableStateOf("") }

    Surface(color = background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp), // гайдлайн 30dp со всех сторон
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Введите\nномер телефона",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 38.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Чтобы войти или стать клиентом",
                color = hint,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(24.dp))

            PhoneNumberField(
                valueDigits = digits,
                onDigitsChange = { if (it.length <= 10) digits = it },
                containerColor = fieldBg,
                hintColor = hint
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onContinue("+7$digits") },
                enabled = digits.length == 10,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonPurple,
                    contentColor = Color.White,
                    disabledContainerColor = buttonPurple.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Text("Продолжить", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }


        }
    }
}

@Composable
private fun PasswordEntryScreen(
    phone: String,
    onSignIn: (token: String) -> Unit,
    onBack: () -> Unit
) {
    val background = AppBg
    val fieldBg = Color(0xFF1C1C1E)
    val hint = Color(0x99FFFFFF)
    val buttonPurple = Color(0xFF8B7CF6)

    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var tried by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val valid = remember(password) { isPasswordStrong(password) }
    val helpColor = if (tried && !valid) Color(0xFFFF4D4F) else hint

    Surface(color = background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            horizontalAlignment = Alignment.Start
        ) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Text("Назад", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Введите пароль",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 38.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(text = phone, color = hint, fontSize = 16.sp)

            Spacer(Modifier.height(24.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Пароль", color = hint) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                    disabledContainerColor = fieldBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "8 символов минимум. Латиница, цифры и спец. символы",
                color = helpColor,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    tried = true
                    if (valid && !isLoading) {
                        scope.launch {
                            isLoading = true
                            errorText = null
                            try {
                                val token = ApiService.login(phone, password)
                                onSignIn(token)
                            } catch (e: Exception) {
                                errorText = "Ошибка входа: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = password.isNotBlank() && !isLoading,

            ) {
            Text(if (isLoading) "Входим..." else "Войти",)
        }

            if (errorText != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorText!!, color = Color(0xFFFF4D4F), fontSize = 13.sp)
            }
        }
    }
}
/** Проверка пароля: длина >= 8, хотя бы одна латинская буква, цифра и спец. символ. */
private fun isPasswordStrong(pw: String): Boolean {
    if (pw.length < 8) return false
    val hasLatin = pw.any { it in 'a'..'z' || it in 'A'..'Z' }
    val hasDigit = pw.any { it.isDigit() }
    val hasSpecial = pw.any { !it.isLetterOrDigit() }
    return hasLatin && hasDigit && hasSpecial
}

@Composable
private fun WelcomeScreen(onContinue: () -> Unit) {
    // Colors used on the mock
    val background = AppBg
    val titleColor = Color.White
    val subtitleColor = Color(0x99FFFFFF)
    val buttonPurple = Color(0xFF8B7CF6)

    Surface(color = background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Вы успешно вошли в\nФИНПУЛЬС",
                color = titleColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 38.sp
            )

            // Push the image roughly to the center area similar to the mock
            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // occupy the available vertical space so button sits near bottom
                contentAlignment = Alignment.TopCenter
            ) {
                // Use the provided 302x302 image. The drawable resource name used below is
                // R.drawable.checkmark — add your image under res/drawable/checkmark.png
                Image(
                    painter = painterResource(id = R.drawable.checkmark),
                    contentDescription = "check",
                    modifier = Modifier
                        .size(302.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(12.dp))

            // Rounded full-width button at the bottom (respecting 30dp padding on sides)
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonPurple,
                    contentColor = Color.Black,
                    disabledContainerColor = buttonPurple.copy(alpha = 0.4f),
                    disabledContentColor = Color.Black.copy(alpha = 0.6f)
                ),
                contentPadding = PaddingValues()
            ) {
                Text(
                    text = "Продолжить",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PhoneNumberField(
    valueDigits: String,
    onDigitsChange: (String) -> Unit,
    containerColor: Color,
    hintColor: Color
) {
    TextField(
        value = valueDigits,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }
            onDigitsChange(digitsOnly.take(10))
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Номер телефона", color = hintColor) },
        leadingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RussiaFlagIcon(size = 22.dp)
                Spacer(Modifier.width(10.dp))
            }
        },
        prefix = { Text("+7 ", color = Color.White) },
        visualTransformation = PhoneNumberVisualTransformation(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
        )
    )
}

/** Кружок-флаг РФ без ресурсов. */
@Composable
private fun RussiaFlagIcon(size: Dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = Color.Transparent
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().background(Color.White))
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF0039A6))) // синий
            Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFD52B1E))) // красный
        }
    }
}

/** Форматирование 10 цифр в вид 999 999-99-99. */
private fun formatRuPhone(digits: String): String {
    val seg = intArrayOf(3, 3, 2, 2)
    val sep = arrayOf(" ", " ", "-", "-")
    val sb = StringBuilder()
    var p = 0
    for (i in seg.indices) {
        if (p >= digits.length) break
        val take = minOf(seg[i], digits.length - p)
        sb.append(digits.substring(p, p + take))
        p += take
        if (take == seg[i] && p < digits.length) sb.append(sep[i])
    }
    return sb.toString()
}

private class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(10)
        val formatted = formatRuPhone(raw)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                val sub = raw.take(clamped)
                return formatRuPhone(sub).length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                val visibleSub = formatted.take(clamped)
                return visibleSub.count { it.isDigit() }.coerceIn(0, raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/** Форматируем "+7XXXXXXXXXX" в вид "+7 (999) 100 10 10" для шапки профиля. */
private fun formatE164ForProfile(phoneE164: String): String {
    val digits = phoneE164.filter { it.isDigit() }
    val core = if (digits.startsWith("7") && digits.length >= 11) digits.takeLast(10) else digits.takeLast(10)
    if (core.length != 10) return phoneE164
    val p1 = core.substring(0, 3)
    val p2 = core.substring(3, 6)
    val p3 = core.substring(6, 8)
    val p4 = core.substring(8, 10)
    return "+7 ($p1) $p2 $p3 $p4"
}

@Preview(showBackground = true, backgroundColor = 0x000000, widthDp = 392, heightDp = 852)
@Composable
private fun PhoneEntryPreview() {
    PhoneEntryScreen()
}
@Composable
private fun CardChallengesScreen(
    accountId: String,
    onBack: () -> Unit = {}
) {
    val account = remember(accountId) {
        BankRepository.getAccountsUi().find { it.id == accountId }
    }

    val balanceText = remember(account) {
        val value = account?.balance ?: 0.0
        // очень простое форматирование, при желании можно улучшить
        String.format("%,.2f ₽", value).replace(",", " ")
    }

    val cardImageRes = when (account?.bank?.lowercase()) {
        "vbank" -> R.drawable.vbank_card
        "abank" -> R.drawable.abank_card
        "sbank" -> R.drawable.sbank_card
        else -> R.drawable.card_blue
    }
    val background = AppBg
    val panelBg = Color(0xFF1C1C1E)
    val itemBg = Color(0xFF2B2B2B)
    val hint = Color(0x99FFFFFF)

    Surface(color = background) {
        Box(Modifier.fillMaxSize()) {
            // soft glow in the top-right corner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x8840E0D0), Color.Transparent),
                            center = Offset.Infinite.copy(x = Float.POSITIVE_INFINITY, y = 0f),
                            radius = 400f
                        )
                    )
            )

            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = 30.dp)
                    .padding(top = 12.dp, bottom = 30.dp)
            ) {
                // Top bar with back
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }

                Spacer(Modifier.height(8.dp))

                Text("Ваша карта", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = balanceText,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                // Card row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Preview card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(104.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF202020)
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = cardImageRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(width = 160.dp, height = 100.dp)
                                    .align(Alignment.CenterStart)
                                    .padding(start = 12.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    }

                    // Add tile
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(104.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF202020)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Add, contentDescription = "Добавить", tint = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Challenges panel
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = panelBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Челленджи основаны на анализе ваших трат",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.Close, contentDescription = "Закрыть", tint = Color.White.copy(0.9f))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        ChallengeItem(icon = Icons.Filled.LocalCafe, text = "7 дней без кофе", bg = itemBg)
                        ChallengeItem(icon = Icons.Filled.DirectionsWalk, text = "Пройтись пешком вместо автобуса", bg = itemBg)
                        ChallengeItem(icon = Icons.Filled.Block, text = "Не покупать 3я месяц", bg = itemBg)
                        ChallengeItem(icon = Icons.Filled.Brush, text = "Воздержаться от покупок в Леонардо", bg = itemBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeItem(icon: ImageVector, text: String, bg: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0xFF343434)) {
                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
            Text(text = text, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun SBankConsentScreen(
    onBack: () -> Unit,
    onConsentReady: (String) -> Unit
) {
    val background = AppBg
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(1) } // 1 - отправка запроса, 2 - ожидание подтверждения
    var requestId by remember { mutableStateOf<String?>(null) }
    var consentId by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Surface(color = background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 20.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }
                Text(
                    text = "Подключить SBank",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(24.dp))

            when {
                consentId != null -> {
                    Text(
                        text = "SBank успешно подключён 🎉",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Теперь мы сможем подтягивать ваши счета и транзакции из SBank.",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            // сохраняешь где нужно и выходишь
                            onConsentReady(consentId!!)
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Готово")
                    }
                }

                step == 1 -> {
                    Text(
                        text = "Шаг 1 из 2",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Отправим запрос на доступ к счетам в SBank",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Мы создадим согласие в SBank. После этого зайдите в интернет-банк и подтвердите его.",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    if (errorText != null) {
                        Text(
                            text = errorText!!,
                            color = Color(0xFFFF4D4F),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorText = null
                                try {
                                    val resp = ApiService.createAccountsConsent("sbank")
                                    when {
                                        resp.consent_id != null -> {
                                            // банк сразу вернул consent_id
                                            consentId = resp.consent_id
                                            // можно сразу дернуть onConsentReady, но проще оставить пользователю "Готово"
                                        }
                                        resp.request_id != null -> {
                                            requestId = resp.request_id
                                            step = 2
                                        }
                                        else -> {
                                            errorText = "Сервер не вернул request_id/consent_id"
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorText = e.message ?: "Неизвестная ошибка"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Отправить запрос в SBank")
                    }
                }

                step == 2 -> {
                    Text(
                        text = "Шаг 2 из 2",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Подтверди согласие в SBank",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "1. Открой интернет-банк SBank (в браузере или приложении)\n" +
                                "2. Найди раздел «Согласия»\n" +
                                "3. Подтверди запрос от «Team 255 App»\n" +
                                "4. Вернись сюда и нажми кнопку ниже",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    requestId?.let {
                        Text(
                            text = "ID запроса: $it",
                            color = Color(0xFF666666),
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    if (errorText != null) {
                        Text(
                            text = errorText!!,
                            color = Color(0xFFFF4D4F),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            val rid = requestId ?: return@Button
                            scope.launch {
                                isLoading = true
                                errorText = null
                                try {
                                    val resp = ApiService.getConsentByRequestId("sbank", rid)
                                    if (resp.consent_id != null) {
                                        consentId = resp.consent_id
                                    } else {
                                        errorText = "Согласие ещё не подтверждено. Попробуйте чуть позже."
                                    }
                                } catch (e: Exception) {
                                    errorText = e.message ?: "Неизвестная ошибка"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Я подтвердил в SBank")
                    }
                }
            }
        }
    }
}
