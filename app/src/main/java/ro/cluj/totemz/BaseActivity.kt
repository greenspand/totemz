package ro.cluj.totemz


import android.app.NotificationManager
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import com.google.android.gms.maps.model.LatLng
import com.greenspand.kotlin_ext.snack
import rx.functions.Action1
import rx.subscriptions.CompositeSubscription

/**
 * Created by sorin on 8/21/16.
 */
abstract class BaseActivity : AppCompatActivity(), KodeinInjected {

    abstract fun getActivityTitle(): Int

    abstract fun getRootLayout(): View

    override val injector = KodeinInjector()

    //Inject components
    val notificationManager: NotificationManager by withContext(this).instance()
    val sharedPrefs: SharedPreferences by withContext(this).instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appKodein())
    }

    fun getRxBusObserver(): Action1<Any> {
        return Action1 { event ->
            if (event is LatLng) {
                snack(getRootLayout(), "Location is: ${event.latitude} ${event.longitude}")
            }
        }
    }
    override fun setTitle(title: CharSequence) {
        super.setTitle(getActivityTitle())
    }

}
