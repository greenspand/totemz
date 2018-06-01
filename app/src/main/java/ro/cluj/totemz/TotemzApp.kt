package ro.cluj.totemz

/* ktlint-disable no-wildcard-imports */

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import com.facebook.FacebookSdk
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.androidModule
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.lazy
import com.github.salomonbrys.kodein.singleton
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import net.hockeyapp.android.CrashManager
import ro.cluj.totemz.firebase.firebaseModule
import ro.cluj.totemz.mqtt.mqttModule
import ro.cluj.totemz.screens.screensModule

/**
 * Created by Sorin Albu-Irimies on 8/27/2016.
 */
open class TotemzApp : MultiDexApplication(), KodeinAware {

  companion object {
    init {
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }
  }

  override val kodein by Kodein.lazy {
    bind<Application>() with singleton { this@TotemzApp }
    bind<Context>() with singleton { applicationContext }
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
    /*Twitter*/
    val authConfig = TwitterConfig.Builder(this).logger(DefaultLogger(Log.DEBUG))
        .twitterAuthConfig(TwitterAuthConfig(getString(R.string.twitter_key),
            getString(R.string.twitter_secret))).build()
    Twitter.initialize(authConfig)
  }
}