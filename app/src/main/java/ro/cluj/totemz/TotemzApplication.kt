package ro.cluj.totemz

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidModule
import com.karumi.dexter.Dexter
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import ro.cluj.totemz.screens.mapModule
import ro.cluj.totemz.utils.RxBus
import ro.cluj.totemz.utils.realmConfiguration
import timber.log.Timber

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
        Realm.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            if (LeakCanary.isInAnalyzerProcess(this)) {
                /**This process is dedicated to LeakCanary for heap analysis.
                You should not init your app in this process.*/
                return
            }
            LeakCanary.install(this)
        }

        val config = realmConfiguration {
            schemaVersion(SCHEMA_VERSION)
            deleteRealmIfMigrationNeeded()
        }

        Realm.setDefaultConfiguration(config)
    }
}