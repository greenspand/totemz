package ro.cluj.totemz.models.adapters

/* ktlint-disable no-wildcard-imports */

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import ro.cluj.totemz.models.User
import ro.cluj.totemz.models.UserGroup

object MoshiAdapters {
    val moshi: Moshi by lazy {
        Moshi.Builder()
                .add(LocationAdapter())
                .add(CurrencyAdapter())
                .build()
    }

    val UserGroup: JsonAdapter<UserGroup> = adapter()
    val User: JsonAdapter<User> = adapter()

    private inline fun <reified T> adapter(): JsonAdapter<T>
            = moshi.adapter(T::class.java)
}