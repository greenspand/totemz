package ro.cluj.totemz.screens

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.animation.BounceInterpolator
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.greenspand.kotlin_ext.snack
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.BaseFragAdapter
import ro.cluj.totemz.R
import ro.cluj.totemz.model.FragmentTypes
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.TotemzUser
import ro.cluj.totemz.model.UserGroup
import ro.cluj.totemz.mqtt.MQTTService
import ro.cluj.totemz.mqtt.MqttBroadcastReceiver
import ro.cluj.totemz.screens.camera.FragmentCamera
import ro.cluj.totemz.screens.map.FragmentMap
import ro.cluj.totemz.screens.user.FragmentUser
import ro.cluj.totemz.screens.user.UserLoginActivity
import ro.cluj.totemz.utils.FadePageTransformer
import timber.log.Timber

class TotemzBaseActivity : BaseActivity(), ViewPager.OnPageChangeListener, OnFragmentActionsListener, TotemzBaseView, MqttBroadcastReceiver.Receiver {

    val SERVICE_CLASSNAME = "ro.cluj.totemz.mqtt.MQTTService"
    private var isLoggedIn = false

    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private val firebaseDB: () -> FirebaseDatabase by provider()
    private val firebaseUserGroup: DatabaseReference by lazy { firebaseDB.invoke().getReference("userGroups") }
    private val receiver by lazy { MqttBroadcastReceiver() }


    //Animation properties
    val SCALE_UP = 1f
    val SCALE_DOWN = 0.7f
    val DURATION = 300L
    val TAG = TotemzBaseActivity::class.java.simpleName

    //Subscriptions
    val TAB_CAMERA = 0
    val TAB_MAP = 1
    val TAB_USER = 2
    //Injections
    val presenter: TotemzBasePresenter by instance()
    val activityManager: ActivityManager by withContext(this).instance()
    private val disposables by lazy { CompositeDisposable() }
    @StringRes
    override fun getActivityTitle(): Int {
        return R.string.app_name
    }


    val RC_LOGIN = 145

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)
        /*Instantiate MQTT BroadcastReceiver*/
        receiver.setReceiver(this@TotemzBaseActivity)

        /* Instantiate pager adapter and set fragments*/
        val adapter = BaseFragAdapter(supportFragmentManager,
                arrayListOf(FragmentCamera.newInstance(), FragmentMap.newInstance(), FragmentUser.newInstance()))

        /*set custom trnasformer for fading text view*/
        pager_menu_switch.setPageTransformer(true, FadePageTransformer())
        pager_menu_switch.adapter = adapter

        /*Set ofscreen page limit for fragment state retention*/
        pager_menu_switch.offscreenPageLimit = 3
        pager_menu_switch.addOnPageChangeListener(this)

        // Set menu click listeners
        img_camera.setOnClickListener {
            pager_menu_switch.currentItem = TAB_CAMERA
            disposables.add(scaleCameraAnim())
            cont_pulse_camera.start()
            cont_pulse_compass.stop()
        }

        img_compass.setOnClickListener {
            pager_menu_switch.currentItem = TAB_MAP
            disposables.add(scaleMapAnim())
            cont_pulse_compass.start()

        }

        img_user.setOnClickListener {
            pager_menu_switch.currentItem = TAB_USER
            disposables.add(scaleUserAnim())
        }

        pager_menu_switch.currentItem = TAB_MAP

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                for (userInfo in user.providerData) {
                    when (userInfo.providerId) {
                        "google.com" -> {
                            val token = sharedPrefs.invoke().getString("GOOGLE_TOKEN", user.getToken(true).toString())
//                                SyncUser.loginAsync(SyncCredentials.google(token), AUTH_URL, this@TotemzBaseActivity)
                        }
                        "facebook.com" -> {
                            val token = sharedPrefs.invoke().getString("FACEBOOK_TOKEN", user.getToken(true).toString())
//                                SyncUser.loginAsync(SyncCredentials.facebook(token), AUTH_URL, this@TotemzBaseActivity)
                        }
                    }
                }
                if (!serviceIsRunning()) {
                    startService(Intent(this, MQTTService::class.java))
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
                    val filter = IntentFilter(MQTTService.ACTION_SHUTTLE_LOCATION)
                    LocalBroadcastManager.getInstance(this@TotemzBaseActivity).registerReceiver(receiver, filter)
                }
            } else {
                startActivityForResult(Intent(this, UserLoginActivity::class.java), RC_LOGIN)
                isLoggedIn = false
                // User is signed out
                Timber.i("onAuthStateChanged:signed_out")
            }
        }

    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MQTTService.ACTION_SHUTTLE_LOCATION -> {
                val shuttleLocation = intent.getSerializableExtra(MQTTService.PARAM_SHUTTLE_LOCATION) as FriendLocation?
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


    private fun stopMQTTLocationService() {
        stopService(Intent(this, MQTTService::class.java))
        LocalBroadcastManager.getInstance(this@TotemzBaseActivity).unregisterReceiver(receiver)
    }

    private fun serviceIsRunning(): Boolean {
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (SERVICE_CLASSNAME == service.service.className) {
                Log.i("MQTT", "SERVICE IS RUNNING")
                return true
            }
        }
        Log.i("MQTT", "SERVICE HAS STOPPED")
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_LOGIN) {
            if (!serviceIsRunning()) {
                startService(Intent(this, MQTTService::class.java))
            }
            val user = firebaseAuth.invoke().currentUser

            //TODO this is just a test to see if a group gets created and then retrieved
            firebaseUserGroup.setValue(UserGroup("PrimeGroup", TotemzUser(user?.email, user?.displayName, null)
                    , arrayListOf(TotemzUser("whatever@yahoo.com", "Giusi", null)
                    , TotemzUser("mastersorini@yahoo.com", "Sorin", null))))
            firebaseUserGroup.push()
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
        presenter.detachView()
        disposables.dispose()
        if (serviceIsRunning()) {
            stopMQTTLocationService()
        }
        super.onDestroy()
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(pos: Int) {
        when (pos) {
            TAB_CAMERA -> {
                scaleCameraAnim()
                cont_pulse_camera.start()
                cont_pulse_compass.stop()
                cont_pulse_user.stop()
            }
            TAB_MAP -> {
                scaleMapAnim()
                cont_pulse_camera.stop()
                cont_pulse_compass.start()
                cont_pulse_user.stop()
            }
            TAB_USER -> {
                scaleUserAnim()
                cont_pulse_camera.stop()
                cont_pulse_compass.stop()
                cont_pulse_user.start()
            }
        }
    }

    fun scaleCameraAnim() = presenter.scaleAnimation(arrayListOf(img_camera), SCALE_UP, DURATION,
            BounceInterpolator()).mergeWith(
            presenter.scaleAnimation(arrayListOf(img_compass, img_user), SCALE_DOWN,
                    DURATION, BounceInterpolator()))
            .subscribe()

    fun scaleMapAnim() = presenter.scaleAnimation(arrayListOf(img_compass), SCALE_UP, DURATION,
            BounceInterpolator())
            .mergeWith(presenter.scaleAnimation(arrayListOf(img_camera, img_user), SCALE_DOWN, DURATION,
                    BounceInterpolator()))
            .subscribe()

    fun scaleUserAnim() = presenter.scaleAnimation(arrayListOf(img_user), SCALE_UP, DURATION,
            BounceInterpolator())
            .mergeWith(presenter.scaleAnimation(arrayListOf(img_camera, img_compass), SCALE_DOWN, DURATION,
                    BounceInterpolator()))
            .subscribe()


}

