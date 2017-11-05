package ro.cluj.totemz

import android.content.Context
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidModule
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import net.hockeyapp.android.CrashManager
import ro.cluj.totemz.firebase.firebaseModule
import ro.cluj.totemz.mqtt.mqttModule
import ro.cluj.totemz.screens.screensModule

/**
 * Created by Sorin Albu-Irimies on 8/27/2016.
 */
open class TotemzApp : MultiDexApplication(), KodeinAware {

    companion object {
        val compat = AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override val kodein by Kodein.lazy {
        bind<Context>() with singleton { applicationContext }
        bind<Realm>() with singleton { Realm.getDefaultInstance() }
        import(screensModule)
        import(mqttModule)
        import(androidModule)
        import(firebaseModule)
    }

    override fun onCreate() {
        super.onCreate()

        /*HockeyApp*/
        CrashManager.register(this)

        /*Facebook*/
        FacebookSdk.sdkInitialize(this)
        /*Facebook event logger*/
        AppEventsLogger.activateApp(this)

        /*Twitter*/
        val authConfig = TwitterAuthConfig(getString(R.string.twitter_key),
                getString(R.string.twitter_secret))
        Fabric.with(this, Twitter(authConfig))
        Fabric.with(this@TotemzApp, Crashlytics())

        /*Realm*/
        Realm.init(this)
    }
}