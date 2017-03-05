package ro.cluj.totemz.utils

import io.realm.RealmConfiguration

/**
 * Realm related extension function expressions
 * Created by sorin on 19.11.16.
 */



inline fun realmConfiguration(func : RealmConfiguration.Builder.() -> Unit) : RealmConfiguration {
    val builder = RealmConfiguration.Builder()
    builder.func()
    return builder.build()
}
