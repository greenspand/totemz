package ro.cluj.totemz.map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.StringRes
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import com.github.salomonbrys.kodein.instance
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.greenspand.kotlin_ext.snack
import kotlinx.android.synthetic.main.activity_main.*
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.R
import ro.cluj.totemz.utils.ExpandViewsOnSubscribe
import ro.cluj.totemz.utils.withPermissionGranted
import rx.Completable
import rx.subscriptions.CompositeSubscription

class TotemzMapActivity : BaseActivity(), TotemzMapView, OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    lateinit var locationManager: LocationManager
    lateinit var totemzMarker: Marker
    lateinit var markerOptions: MarkerOptions
    lateinit var googleMap: GoogleMap
    lateinit var googleApiClient: GoogleApiClient

    var isMapReady = false

    // Map proerties
    val DEFAULT_ZOOM = 16f
    var CAMERA_REQUEST = 93

    //Animation properties
    val SCALE_UP = 1f
    val SCALE_DOWN = 0.7f
    val DURATION = 300L

    //Subscriptions
    val compositeSubscription = CompositeSubscription()

    //Injection
    val presenter: TotemzMapPresenter by instance()

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

        //Init Location Service
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Set menu click listeners
        img_camera.setOnClickListener {
            compositeSubscription.add(presenter.scaleAnimation(arrayListOf(img_camera), SCALE_UP, DURATION, BounceInterpolator())
                    .mergeWith(presenter.scaleAnimation(arrayListOf(img_compass, img_user), SCALE_DOWN, DURATION, BounceInterpolator()))
                    .subscribe({
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA_REQUEST)
                    }))
        }

        img_user.setOnClickListener {
            compositeSubscription.add(presenter.scaleAnimation(arrayListOf(img_user), SCALE_UP, DURATION, BounceInterpolator())
                    .mergeWith(presenter.scaleAnimation(arrayListOf(img_camera, img_compass), SCALE_DOWN, DURATION, BounceInterpolator()))
                    .subscribe())
        }

        img_compass.setOnClickListener {
            compositeSubscription.add(presenter.scaleAnimation(arrayListOf(img_compass), SCALE_UP, DURATION, BounceInterpolator())
                    .mergeWith(presenter.scaleAnimation(arrayListOf(img_camera, img_user), SCALE_DOWN, DURATION, BounceInterpolator()))
                    .subscribe())
        }
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onMapReady(googleMap: GoogleMap) {
        withPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION) {
            googleMap.isMyLocationEnabled = true
        }
        isMapReady = true
        this.googleMap = googleMap
        this.googleMap.onLocationTouched(getString(R.string.app_name), 48.737463, 9.127979)
        markerOptions = MarkerOptions().position(LatLng(48.737463, 9.127979)).icon(BitmapDescriptorFactory.fromResource(R.drawable.overwatch))
        markerOptions.anchor(0.0f, 0.0f)
        totemzMarker = this.googleMap.addMarker(markerOptions)
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(48.737463, 9.127979), DEFAULT_ZOOM))

    }

    fun GoogleMap.onLocationTouched(label: String, lat: Double, lng: Double) {
        this.setOnMapClickListener {
            snack(getRootLayout(), "Fatermans fat")
        }
    }

    override fun onCameraMove() {
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST && resultCode === RESULT_OK) {
//            val photo = data?.extras?.get("data") as Bitmap
        }
    }

    override fun onStart() {
        googleApiClient.connect()
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    override fun onDestroy() {
        presenter.detachView()
        compositeSubscription.unsubscribe()
        super.onDestroy()
    }


}
