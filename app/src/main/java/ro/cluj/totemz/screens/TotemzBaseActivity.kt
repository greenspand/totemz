package ro.cluj.totemz.screens

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.annotation.StringRes
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.view.animation.BounceInterpolator
import com.github.salomonbrys.kodein.provider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.greenspand.kotlin_ext.snack
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.BaseFragAdapter
import ro.cluj.totemz.R
import ro.cluj.totemz.model.FragmentTypes
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.TotemzUser
import ro.cluj.totemz.model.UserGroup
import ro.cluj.totemz.mqtt.MqttBroadcastReceiver
import ro.cluj.totemz.mqtt.TotemzMQTTService
import ro.cluj.totemz.screens.camera.FragmentCamera
import ro.cluj.totemz.screens.map.FragmentMap
import ro.cluj.totemz.screens.user.FragmentUser
import ro.cluj.totemz.screens.user.UserLoginActivity
import ro.cluj.totemz.utils.FadePageTransformer
import timber.log.Timber

class TotemzBaseActivity : BaseActivity(),
        ViewPager.OnPageChangeListener,
        MqttBroadcastReceiver.Receiver,
        OnFragmentActionsListener,
        TotemzBaseView {

    private var isLoggedIn = false


    //Animation properties
    val SCALE_UP = 1f
    val SCALE_DOWN = 0.7f
    val DURATION = 300L

    //MQTT Service
    var isBound = false
    var totemzMqttService: TotemzMQTTService? = null

    //TABS
    val TAB_CAMERA = 0
    val TAB_MAP = 1
    val TAB_USER = 2

    //Injections
    val presenter: () -> TotemzBasePresenter by provider()

    private val firebaseUserGroup: DatabaseReference by lazy { firebaseDB.invoke().getReference("userGroups") }
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private val receiver by lazy { MqttBroadcastReceiver() }
    private val disposables by lazy { CompositeDisposable() }

    @StringRes
    override fun getActivityTitle(): Int {
        return R.string.app_name
    }


    val RC_LOGIN = 145

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.invoke().attachView(this)

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
                /*Bind the MQTT service*/
                bindService(Intent(this, TotemzMQTTService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
            } else {
                startActivityForResult(Intent(this, UserLoginActivity::class.java), RC_LOGIN)
                isLoggedIn = false
                // User is signed out
                Timber.i("onAuthStateChanged:signed_out")
            }
        }

        /*Instantiate MQTT BroadcastReceiver*/
        receiver.setReceiver(this@TotemzBaseActivity)
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


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            isBound = true

            /*We've bound to LocalService, cast the IBinder and get LocalService instance*/
            val binder = service as TotemzMQTTService.LocalBinder
            totemzMqttService = binder.service
            val filter = IntentFilter(TotemzMQTTService.ACTION_FRIEND_LOCATION)
            LocalBroadcastManager.getInstance(this@TotemzBaseActivity).registerReceiver(receiver, filter)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            isBound = false
            LocalBroadcastManager.getInstance(this@TotemzBaseActivity).unregisterReceiver(receiver)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_LOGIN) {
            /*Bind the MQTT service*/
            bindService(Intent(this, TotemzMQTTService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)

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
        super.onDestroy()
        presenter.invoke().detachView()
        disposables.dispose()
        /*Unbind from the service*/
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
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

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TotemzMQTTService.ACTION_FRIEND_LOCATION -> {
                val friendLocation = intent.getSerializableExtra(
                        TotemzMQTTService.PARAM_FRIEND_LOCATION) as FriendLocation
                Timber.i("Friend location", "From broadcast receiver is: ",
                        "${friendLocation.location.latitude} ${friendLocation.location.longitude}")
            }
        }
    }

    fun scaleCameraAnim(): Disposable = presenter.invoke().scaleAnimation(arrayListOf(img_camera), SCALE_UP, DURATION,
            BounceInterpolator()).mergeWith(
            presenter.invoke().scaleAnimation(arrayListOf(img_compass, img_user), SCALE_DOWN,
                    DURATION, BounceInterpolator()))
            .subscribe()

    fun scaleMapAnim(): Disposable = presenter.invoke().scaleAnimation(arrayListOf(img_compass), SCALE_UP, DURATION,
            BounceInterpolator())
            .mergeWith(presenter.invoke().scaleAnimation(arrayListOf(img_camera, img_user), SCALE_DOWN, DURATION,
                    BounceInterpolator()))
            .subscribe()

    fun scaleUserAnim(): Disposable = presenter.invoke().scaleAnimation(arrayListOf(img_user), SCALE_UP, DURATION,
            BounceInterpolator())
            .mergeWith(presenter.invoke().scaleAnimation(arrayListOf(img_camera, img_compass), SCALE_DOWN, DURATION,
                    BounceInterpolator()))
            .subscribe()


}

