package ro.cluj.totemz

import com.squareup.leakcanary.LeakCanary
import timber.log.Timber

/**
 * Created by mihai on 3/4/2017.
 */
class DebugTotemzApp :TotemzApp() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            if (LeakCanary.isInAnalyzerProcess(this)) {
                /**This process is dedicated to LeakCanary for heap analysis.
                You should not init your app in this process.*/
                return
            }
            LeakCanary.install(this)
        }
    }
}