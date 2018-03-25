package ro.cluj.totemz.screens

/* ktlint-disable no-wildcard-imports */

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.view.ViewPager
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.greenspand.kotlin_ext.snack
import kotlinx.android.synthetic.main.activity_main.*
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.BaseFragAdapter
import ro.cluj.totemz.R
import ro.cluj.totemz.models.FragmentTypes
import ro.cluj.totemz.models.User
import ro.cluj.totemz.models.UserGroup
import ro.cluj.totemz.mqtt.MQTTService
import ro.cluj.totemz.screens.camera.CameraFragment
import ro.cluj.totemz.screens.map.MapFragment
import ro.cluj.totemz.screens.user.UserViewFragment
import ro.cluj.totemz.screens.user.login.UserLoginViewActivity
import ro.cluj.totemz.utils.FadePageTransformer
import timber.log.Timber

private const val TAB_CAMERA = 0
private const val TAB_MAP = 1
private const val TAB_USER = 2
private const val RC_LOGIN = 145

class TotemzBaseActivity : BaseActivity(), ViewPager.OnPageChangeListener, FragmentsActionsListener, TotemzBaseView {

    private val SERVICE_CLASSNAME = "ro.cluj.totemz.mqtt.MQTTService"
    private var isLoggedIn = false
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private val firebaseDB: () -> FirebaseDatabase by provider()
    private val firebaseUserGroup: DatabaseReference by lazy { firebaseDB.invoke().getReference("userGroups") }
    private val presenter: TotemzBasePresenter by instance()
    private val activityManager: ActivityManager by withContext(this).instance()

    @StringRes override fun getActivityTitle() = R.string.app_name

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
            addOnPageChangeListener(this@TotemzBaseActivity)
        }

        // Set menu click listeners
        img_camera.setOnClickListener {
            pager_menu_switch.currentItem = TAB_CAMERA
            cont_pulse_camera.start()
            cont_pulse_compass.stop()
        }

        img_compass.setOnClickListener {
            pager_menu_switch.currentItem = TAB_MAP
            cont_pulse_compass.start()

        }

        img_user.setOnClickListener {
            pager_menu_switch.currentItem = TAB_USER
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
//                    startService(Intent(this, MQTTService::class.java))
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

    //TODO USE IT!
    override fun onNextFragment(fragType: FragmentTypes) {
        when (fragType) {
            FragmentTypes.FRAG_CAM -> {

            }
            FragmentTypes.FRAG_MAP -> {

            }
            FragmentTypes.FRAG_USER -> {
            }
        }
    }

    override fun showFriendLocation(user: User) {

    }

    private fun stopMQTTLocationService() {
        stopService(Intent(this, MQTTService::class.java))
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
                startService(Intent(this, MQTTService::class.java))
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

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(pos: Int) {
        when (pos) {
            TAB_CAMERA -> {
                cont_pulse_camera.start()
                cont_pulse_compass.stop()
                cont_pulse_user.stop()
            }
            TAB_MAP -> {
                cont_pulse_camera.stop()
                cont_pulse_compass.start()
                cont_pulse_user.stop()
            }
            TAB_USER -> {
                cont_pulse_camera.stop()
                cont_pulse_compass.stop()
                cont_pulse_user.start()
            }
        }
    }
}

