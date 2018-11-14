package ro.cluj.totemz

import android.app.Application

class TotemzApp : Application() {

    override fun onCreate() {
        super.onCreate()
        /*HockeyApp*/
//        CrashManager.register(this)
//        /*Facebook*/
//        FacebookSdk.sdkInitialize(this)
//        /*Twitter*/
//        val authConfig = TwitterConfig.Builder(this).logger(DefaultLogger(Log.DEBUG))
//            .twitterAuthConfig(
//                TwitterAuthConfig(
//                    getString(R.string.twitter_key),
//                    getString(R.string.twitter_secret)
//                )
//            ).build()
//        Twitter.initialize(authConfig)
    }
}