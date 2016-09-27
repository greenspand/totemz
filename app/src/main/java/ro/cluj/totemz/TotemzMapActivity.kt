package ro.cluj.totemz

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View
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
import ro.cluj.totemz.map.TotemzMapPresenter
import ro.cluj.totemz.map.TotemzMapView
import ro.cluj.totemz.utils.withPermissionGranted

class TotemzMapActivity : BaseActivity(), TotemzMapView, OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    lateinit var locationManager: LocationManager
    lateinit var totemzMarker: Marker
    lateinit var markerOptions: MarkerOptions
    lateinit var presenter: TotemzMapPresenter
    lateinit var googleMap: GoogleMap
    lateinit var googleApiClient: GoogleApiClient
    var isMapReady = false
    val DEFAULT_ZOOM = 16f

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
        fab.setOnClickListener {
            snack(getRootLayout(), "Location is: Lardland, similar to Wallyland, only fatter :-)").show()
        }
        //init google API client
        googleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build()
        //Get Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frag_map) as SupportMapFragment
        mapFragment.getMapAsync(this@TotemzMapActivity)

        //Init Location Service
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

    }

    override fun onStart() {
        googleApiClient.connect()
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
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
        withPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION) {
            this.googleMap.isMyLocationEnabled = true
        }
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
}
