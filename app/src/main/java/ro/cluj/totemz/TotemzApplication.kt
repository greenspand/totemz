package ro.cluj.totemz

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.androidModule
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.lazy
import io.fabric.sdk.android.Fabric
import ro.cluj.totemz.map.mapModule

/**
 * Created by mihai on 8/27/2016.
 */
class TotemzApplication : Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        import(mapModule)
        import(androidModule)
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