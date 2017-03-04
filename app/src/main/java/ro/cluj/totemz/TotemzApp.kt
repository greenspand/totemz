package ro.cluj.totemz

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidModule
import com.karumi.dexter.Dexter
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import ro.cluj.totemz.screens.mapModule
import ro.cluj.totemz.utils.RxBus
import ro.cluj.totemz.utils.realmConfiguration

/**
 * Created by mihai on 8/27/2016.
 */
open class TotemzApp : Application(), KodeinAware {


    override val kodein by Kodein.lazy {
        bind<RxBus>() with singleton { RxBus }
        bind<Realm>() with singleton { Realm.getDefaultInstance() }
        import(mapModule)
        import(androidModule)
    }

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this@TotemzApp, Crashlytics())
        Dexter.initialize(this)
        Realm.init(this)

        val config = realmConfiguration {
            val SCHEMA_VERSION: Long = 0
            schemaVersion(SCHEMA_VERSION)
            deleteRealmIfMigrationNeeded()
        }

        Realm.setDefaultConfiguration(config)
    }
}