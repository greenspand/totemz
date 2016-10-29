package ro.cluj.totemz.map

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.StringRes
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import com.github.salomonbrys.kodein.android.withContext
import com.github.salomonbrys.kodein.instance
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.intentFor
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.R
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.MyLocation
import ro.cluj.totemz.mqtt.MQTTService
import rx.subscriptions.CompositeSubscription

class TotemzMapActivity : BaseActivity(), TotemzMapView, OnMapReadyCallback, PermissionListener,
        GoogleMap.OnCameraMoveListener,
        LocationSource.OnLocationChangedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    lateinit var totemzMarker: Marker
    lateinit var markerOptions: MarkerOptions
    var googleMap: GoogleMap? = null
    lateinit var googleApiClient: GoogleApiClient

    val SERVICE_CLASSNAME = "ro.cluj.totemz.mqtt.MQTTService"


    var isMapReady = false

    // Map proerties
    val DEFAULT_ZOOM = 15f
    var CAMERA_REQUEST = 93

    //Animation properties
    val SCALE_UP = 1f
    val SCALE_DOWN = 0.7f
    val DURATION = 300L

    //Subscriptions
    val compositeSubscription = CompositeSubscription()

    //Injections
    val presenter: TotemzMapPresenter by instance()
    val activityManager: ActivityManager by withContext(this).instance()


    @StringRes
    override fun getActivityTitle(): Int {
        return R.string.app_name
    }

    override fun getRootLayout(): View {
        return container_totem
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)

        //init google API client
        googleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
        //Get Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frag_map) as SupportMapFragment
        mapFragment.getMapAsync(this@TotemzMapActivity)

        // Set menu click listeners
        img_camera.setOnClickListener {
            compositeSubscription.add(presenter.scaleAnimation(arrayListOf(img_camera), SCALE_UP, DURATION, BounceInterpolator())
                    .mergeWith(presenter.scaleAnimation(arrayListOf(img_compass, img_user), SCALE_DOWN, DURATION, BounceInterpolator()))
                    .subscribe({
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST)
                    }))
        }

        img_compass.setOnClickListener {
            compositeSubscription.add(presenter.scaleAnimation(arrayListOf(img_compass), SCALE_UP, DURATION, BounceInterpolator())
                    .mergeWith(presenter.scaleAnimation(arrayListOf(img_camera, img_user), SCALE_DOWN, DURATION, BounceInterpolator()))
                    .subscribe())
            if (!serviceIsRunning()) {
                startService(intentFor<MQTTService>())
            }
        }

        img_user.setOnClickListener {
            compositeSubscription.add(presenter.scaleAnimation(arrayListOf(img_user), SCALE_UP, DURATION, BounceInterpolator())
                    .mergeWith(presenter.scaleAnimation(arrayListOf(img_camera, img_compass), SCALE_DOWN, DURATION, BounceInterpolator()))
                    .subscribe())
            if (serviceIsRunning())
                stopMQTTLOcationService()
        }
        startService(intentFor<MQTTService>())
    }

    //Permission request callback
    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        response?.let {
            if (response.permissionName == Manifest.permission.ACCESS_FINE_LOCATION) {
                googleMap?.let {
                    it.isMyLocationEnabled = true
                    it.uiSettings.isMyLocationButtonEnabled = true
                }
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            it.isMyLocationEnabled = true
            it.uiSettings.isMyLocationButtonEnabled = true
            isMapReady = true
            this.googleMap = googleMap
            val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            location?.let {
                markerOptions = MarkerOptions().position(LatLng(location.latitude, location.longitude)).icon(BitmapDescriptorFactory.fromResource(R.drawable.overwatch))
                markerOptions.anchor(0.0f, 0.0f)
                totemzMarker = this.googleMap?.addMarker(markerOptions) as Marker
                this.googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_ZOOM))
            }
        }
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {
            EventBus.getDefault().post(MyLocation(LatLng(location.latitude, location.longitude)))
        }
    }


    override fun onConnected(connectionHint: Bundle?) {
        val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        location?.let {
            EventBus.getDefault().post(MyLocation(LatLng(location.latitude, location.longitude)))
        }

    }

    override fun onCameraMove() {
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }


    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
    }

    private fun stopMQTTLOcationService() {
        val intent = Intent(this, MQTTService::class.java)
        stopService(intent)
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


    // This method will be called when a MessageEvent is posted (in the UI thread for Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: FriendLocation) {
        val location = event.location
        Log.w("TotemzMapActivity","Location received ${location.latitude} ${location.longitude}")

        //When the map is ready we show the friends position
        googleMap?.let {
            val markerOptions = MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_totemz))
            markerOptions.anchor(0.0f, 0.0f)
            it.addMarker(markerOptions)
            this.googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude,location.longitude), DEFAULT_ZOOM))
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST && resultCode === RESULT_OK) {
//            val photo = data?.extras?.get("data") as Bitmap
        }
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        googleApiClient.disconnect()
    }

    override fun onDestroy() {
        presenter.detachView()
        compositeSubscription.unsubscribe()
        super.onDestroy()
    }
}
