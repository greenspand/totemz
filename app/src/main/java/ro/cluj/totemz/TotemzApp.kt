package ro.cluj.totemz

import android.app.Application
import android.util.Log
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import net.hockeyapp.android.CrashManager

class TotemzApp : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashManager.register(this)
        val authConfig = TwitterConfig.Builder(this).logger(DefaultLogger(Log.DEBUG))
            .twitterAuthConfig(
                TwitterAuthConfig(
                    getString(R.string.twitter_key),
                    getString(R.string.twitter_secret)
                )
            ).build()
        Twitter.initialize(authConfig)
    }

}