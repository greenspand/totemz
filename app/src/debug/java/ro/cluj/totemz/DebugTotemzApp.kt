package ro.cluj.totemz

import android.util.Log
import com.squareup.leakcanary.LeakCanary
import io.realm.log.RealmLog
import timber.log.Timber

/**
 * Created by mihai on 3/4/2017.
 */
class DebugTotemzApp : TotemzApp() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            RealmLog.setLevel(Log.VERBOSE)
            Timber.plant(Timber.DebugTree())
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return
            }
            LeakCanary.install(this)
        }
    }
}