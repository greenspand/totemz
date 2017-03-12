package ro.cluj.totemz

import android.app.Application
import android.content.Context
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidModule
import com.google.firebase.auth.FirebaseAuth
import com.karumi.dexter.Dexter
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import ro.cluj.totemz.screens.mapModule
import ro.cluj.totemz.screens.user.userModule
import ro.cluj.totemz.utils.RxBus
import ro.cluj.totemz.utils.realmConfiguration
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.tweetcomposer.TweetComposer
import com.twitter.sdk.android.tweetui.TweetUi


/**
 * Created by mihai on 8/27/2016.
 */
open class TotemzApp : Application(), KodeinAware {


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
        Fabric.with(this, TwitterCore(authConfig),TweetUi(), TweetComposer())
        Fabric.with(this@TotemzApp, Crashlytics())

        /*Runtime permissions*/
        Dexter.initialize(this)

        /*Facebook event logger*/
        AppEventsLogger.activateApp(this)

        /*Realm database init.*/
        Realm.init(this)
        val config = realmConfiguration {
            val SCHEMA_VERSION: Long = 0
            schemaVersion(SCHEMA_VERSION)
            deleteRealmIfMigrationNeeded()
        }

        Realm.setDefaultConfiguration(config)
    }
}