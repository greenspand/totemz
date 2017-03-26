package ro.cluj.totemz


import android.app.NotificationManager
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import io.realm.Realm

/**
 * Created by Sorin Albu-Irimies on 8/27/2016.
 */
abstract class BaseActivity : AppCompatActivity(), KodeinInjected {

    abstract fun getActivityTitle(): Int


    override val injector = KodeinInjector()

    //Inject components
    val notificationManager: NotificationManager by withContext(this).instance()
    val sharedPrefs: SharedPreferences by withContext(this).instance()
    val realm: Realm by instance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appKodein())
    }

    override fun setTitle(title: CharSequence) {
        if (getActivityTitle() != 0) {
            super.setTitle(getActivityTitle())
        }
    }
}
