package ro.cluj.totemz

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.fabric.sdk.android.Fabric

/**
 * Created by mihai on 8/27/2016.
 */
class TotemzApplication : Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        bind<LocationManager>() with instance(getSystemService(Context.LOCATION_SERVICE) as LocationManager)
        bind<SharedPreferences>() with instance(PreferenceManager.getDefaultSharedPreferences(this@TotemzApplication))
        bind<NotificationManager>() with instance( getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        bind<RxBus>() with instance( RxBus)
    }

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this@TotemzApplication, Crashlytics())
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}