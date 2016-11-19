package ro.cluj.totemz

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.android.androidModule
import com.github.salomonbrys.kodein.lazy
import com.github.salomonbrys.kodein.singleton
import com.karumi.dexter.Dexter
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import ro.cluj.totemz.map.mapModule
import ro.cluj.totemz.utils.RxBus
import ro.cluj.totemz.utils.realmConfiguration

/**
 * Created by mihai on 8/27/2016.
 */
class TotemzApplication : Application(), KodeinAware {

    val SCHEMA_VERSION: Long = 0

    override val kodein by Kodein.lazy {
        bind<RxBus>() with singleton { RxBus }
        import(mapModule)
        import(androidModule)
    }

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this@TotemzApplication, Crashlytics())
        Dexter.initialize(this)

        val config = realmConfiguration {
            schemaVersion(SCHEMA_VERSION)
            deleteRealmIfMigrationNeeded()
        }
        Realm.setDefaultConfiguration(config)
    }

    override fun onTerminate() {
        super.onTerminate()
    }

}