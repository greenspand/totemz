package ro.cluj.totemz

import android.content.Context
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import ro.cluj.totemz.firebase.firebaseModule


/**
 * Created by Sorin Albu-Irimies on 8/27/2016.
 */
open class TotemzApp : MultiDexApplication(), KodeinAware {

    companion object {
        val compat = AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override val kodein by Kodein.lazy {
        import(mapModule)
        import(userModule)
        import(androidModule)
        import(firebaseModule)
        bind<RxBus>() with singleton { RxBus }
        bind<Realm>() with singleton { Realm.getDefaultInstance() }
        bind<Context>() with singleton { applicationContext }
    }

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(this)
        /*Twitter config.*/
        val authConfig = TwitterAuthConfig(getString(R.string.twitter_key),
                getString(R.string.twitter_secret))
        Fabric.with(this, Twitter(authConfig))
        Fabric.with(this@TotemzApp, Crashlytics())

        /*Facebook event logger*/
        AppEventsLogger.activateApp(this)

        /*Realm database init.*/
        Realm.init(this)
    }
}