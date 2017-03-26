package ro.cluj.totemz

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.facebook.appevents.AppEventsLogger
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidModule
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import ro.cluj.totemz.screens.mapModule
import ro.cluj.totemz.screens.user.userModule
import ro.cluj.totemz.utils.RxBus


/**
 * Created by Sorin Albu-Irimies on 8/27/2016.
 */
open class TotemzApp : Application(), KodeinAware {

    companion object {
        val compat = AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        const val AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth"
        const val REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/userlocation"

    }

    override val kodein by Kodein.lazy {
        import(mapModule)
        import(userModule)
        import(androidModule)
        bind<RxBus>() with singleton { RxBus }
        bind<FirebaseAuth>() with singleton { FirebaseAuth.getInstance() }
        bind<Realm>() with singleton { Realm.getDefaultInstance() }
        bind<Context>() with singleton { applicationContext }
    }

    override fun onCreate() {
        super.onCreate()

        /*Twitter config.*/
        val authConfig = TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret))
        Fabric.with(this, Twitter(authConfig))
        Fabric.with(this@TotemzApp, Crashlytics())

        /*Runtime permissions*/
        Dexter.initialize(this)

        /*Facebook event logger*/
        AppEventsLogger.activateApp(this)

        /*Realm database init.*/
        Realm.init(this)

    }
}