package ro.cluj.totemz

import android.app.Application
import android.provider.Settings
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidModule
import com.karumi.dexter.Dexter
import io.fabric.sdk.android.Fabric
import ro.cluj.totemz.map.mapModule
import ro.cluj.totemz.model.UserInfo
import ro.cluj.totemz.mqtt.mqttModule
import ro.cluj.totemz.utils.RxBus

/**
 * Created by mihai on 8/27/2016.
 */
class TotemzApplication : Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        bind<RxBus>() with singleton { RxBus }
        bind<UserInfo>() with singleton { UserInfo(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)) }
        import(mapModule)
        import(androidModule)
        import(mqttModule)
    }

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this@TotemzApplication, Crashlytics())
        Dexter.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}