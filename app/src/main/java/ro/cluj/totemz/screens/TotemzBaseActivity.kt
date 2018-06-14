package ro.cluj.totemz.screens

/* ktlint-disable no-wildcard-imports */

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.annotation.StringRes
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.greenspand.kotlin_ext.snack
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.launch
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.BaseFragAdapter
import ro.cluj.totemz.R
import ro.cluj.totemz.models.FragmentTypes
import ro.cluj.totemz.models.User
import ro.cluj.totemz.models.UserGroup
import ro.cluj.totemz.mqtt.FirendsLocationService
import ro.cluj.totemz.screens.camera.CameraFragment
import ro.cluj.totemz.screens.map.MapFragment
import ro.cluj.totemz.screens.user.UserViewFragment
import ro.cluj.totemz.screens.user.login.UserLoginViewActivity
import ro.cluj.totemz.utils.EventBus
import ro.cluj.totemz.utils.FadePageTransformer
import ro.cluj.totemz.utils.onPageSelected
import timber.log.Timber

private const val TAB_CHAT = 0
private const val TAB_MAP = 1
private const val TAB_USER = 2
private const val RC_LOGIN = 145

class TotemzBaseActivity : BaseActivity(), TotemzBaseView {

    private val SERVICE_CLASSNAME = "ro.cluj.totemz.mqtt.FirendsLocationService"
    private var isLoggedIn = false
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private val firebaseDB: () -> FirebaseDatabase by provider()
    private val firebaseUserGroup: DatabaseReference by lazy { firebaseDB.invoke().getReference("userGroups") }
    private val presenter: TotemzBasePresenter by instance()
    private val activityManager: ActivityManager by withContext(this).instance()
    val channel = EventBus().asChannel<Any>()

    @StringRes
    override fun getActivityTitle() = R.string.app_name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)

        /* Instantiate pager adapter and set fragments*/
        pager_menu_switch.apply {
            setPageTransformer(true, FadePageTransformer())
            this.adapter = BaseFragAdapter(supportFragmentManager,
                    arrayListOf(CameraFragment.newInstance(), MapFragment.newInstance(), UserViewFragment.newInstance()))
            offscreenPageLimit = 3
            onPageSelected {
                when (it) {
                    TAB_CHAT -> Timber.i("Tab Chat")
                    TAB_MAP -> Timber.i("Tab Map")
                    TAB_USER -> Timber.i("Tab User")
                }
            }
        }
        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_chat -> pager_menu_switch.currentItem = TAB_CHAT
                R.id.action_map -> pager_menu_switch.currentItem = TAB_MAP
                R.id.action_settings -> pager_menu_switch.currentItem = TAB_USER
            }
            return@setOnNavigationItemSelectedListener true
        }
        pager_menu_switch.currentItem = TAB_MAP

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                for (userInfo in user.providerData) {
                    when (userInfo.providerId) {
                        "google.com" -> {
                            val token = sharedPrefs.invoke().getString("GOOGLE_TOKEN", user.getToken(true).toString())
                        }
                        "facebook.com" -> {
                            val token = sharedPrefs.invoke().getString("FACEBOOK_TOKEN", user.getToken(true).toString())
                        }
                    }
                }
                if (!serviceIsRunning()) {
                    startService(Intent(this, FirendsLocationService::class.java))
                    //TODO remove the firebase demo JSON read
                    firebaseUserGroup.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(dataSnapshot: DatabaseError?) {

                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            val value = dataSnapshot?.getValue(UserGroup::class.java)
                            snack(container_totem, "Value is: $value")
                        }
                    })
                }
            } else {
                startActivityForResult(Intent(this, UserLoginViewActivity::class.java), RC_LOGIN)
                isLoggedIn = false
                // User is signed out
                Timber.i("onAuthStateChanged:signed_out")
            }
        }
    }

    override fun showFriendLocation(user: User) {

    }

    private fun stopMQTTLocationService() {
        stopService(Intent(this, FirendsLocationService::class.java))
    }

    private fun serviceIsRunning(): Boolean {
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_CLASSNAME == service.service.className) {
                Timber.w("MQTT Service is running...")
                return true
            }
        }
        Timber.w("MQTT Service has stopped.")
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_LOGIN) {
            if (!serviceIsRunning()) {
                startService(Intent(this, FirendsLocationService::class.java))
            }
            firebaseAuth.invoke().currentUser?.let {
                //TODO this is just a test to see if a group gets created and then retrieved
                val user = User(it.uid, it.displayName, it.displayName, null)
                firebaseUserGroup.setValue(UserGroup("PrimeGroup", user, arrayListOf(user, user)))
                firebaseUserGroup.push()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.invoke().addAuthStateListener(authStateListener)
        launch {
            for (item in channel) {
                when (item) {
                    is Location -> Timber.w("Channel message is: ${item.altitude}")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.invoke().removeAuthStateListener(authStateListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
        if (serviceIsRunning()) {
            stopMQTTLocationService()
        }
    }
}

