package ro.cluj.totemz.models.adapters

/* ktlint-disable no-wildcard-imports */

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

class CurrencyAdapter {
    @FromJson
    fun currencyFromJson(currency: String): Currency = Currency.getInstance(currency)

    @ToJson
    fun currencyToJson(currency: Currency): String = currency.currencyCode
}