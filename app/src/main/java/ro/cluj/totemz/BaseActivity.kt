package ro.cluj.totemz


import android.app.ActivityManager
import android.app.NotificationManager
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.provider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.realm.Realm

/**
 * Created by Sorin Albu-Irimies on 8/27/2016.
 */
abstract class BaseActivity : AppCompatActivity(), LazyKodeinAware {

    abstract fun getActivityTitle(): Int

    override val kodein = LazyKodein(appKodein)

    val firebaseDB: () -> FirebaseDatabase by provider()
    val activityManager: () -> ActivityManager by withContext(this).provider()
    val notificationManager: () -> NotificationManager by withContext(this).provider()
    val sharedPrefs: () -> SharedPreferences by withContext(this).provider()
    val realm: () -> Realm by provider()
    val firebaseAuth: () -> FirebaseAuth by provider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setTitle(title: CharSequence) {
        if (getActivityTitle() != 0) {
            super.setTitle(getActivityTitle())
        }
    }
}
