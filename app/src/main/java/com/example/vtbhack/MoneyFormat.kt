// MoneyFormat.kt
package com.example.vtbhack

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

object MoneyFormat {

    private val formatter: NumberFormat = NumberFormat.getInstance(Locale("ru", "RU")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
        isGroupingUsed = true      // включаем разделители тысяч (1 234 567)
    }

    fun format(amount: Double): String {
        val intValue = amount.roundToInt()
        val raw = formatter.format(intValue)
        // В русской локали часто ставится неразрывный пробел, заменим на обычный
        return raw.replace('\u00A0', ' ')
    }
}

// Удобные экстеншены
fun Double.toRub(): String = MoneyFormat.format(this) + " ₽"
fun Int.toRub(): String = MoneyFormat.format(this.toDouble()) + " ₽"