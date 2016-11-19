package ro.cluj.totemz.utils

import android.app.Notification
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by sorin on 19.11.16.
 */




inline fun realmConfiguration(func : RealmConfiguration.Builder.() -> Unit) : RealmConfiguration {
    val builder = RealmConfiguration.Builder()
    builder.func()
    return builder.build()
}