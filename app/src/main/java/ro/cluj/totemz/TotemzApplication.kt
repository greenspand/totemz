package ro.cluj.totemz

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidModule
import com.karumi.dexter.Dexter
import io.fabric.sdk.android.Fabric
import ro.cluj.totemz.map.mapModule
import ro.cluj.totemz.utils.RxBus

/**
 * Created by mihai on 8/27/2016.
 */
class TotemzApplication : Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        import(mapModule)
        import(androidModule)
        bind<RxBus>() with singleton { RxBus }
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