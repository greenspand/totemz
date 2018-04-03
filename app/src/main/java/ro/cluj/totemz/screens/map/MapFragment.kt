package ro.cluj.totemz.screens.map

/* ktlint-disable no-wildcard-imports */
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.provider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import ro.cluj.totemz.BaseFragment
import ro.cluj.totemz.BasePresenter
import ro.cluj.totemz.R
import ro.cluj.totemz.models.FragmentTypes
import ro.cluj.totemz.screens.camera.CameraFragment
import ro.cluj.totemz.screens.camera.CameraPresenter
import ro.cluj.totemz.screens.camera.CameraView
import ro.cluj.totemz.utils.EventBus
import ro.cluj.totemz.utils.createAndAddMarker
import ro.cluj.totemz.utils.loadMapStyle
import timber.log.Timber

/**
 * Created by sorin on 11.10.16.
 *
 * Copyright (c) 2016 moovel GmbH<br>
 *
 * All rights reserved<br>
<p></p>
 */
class MapFragment : BaseFragment(), PermissionListener, OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        LocationSource.OnLocationChangedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, CameraView {

    var googleMap: GoogleMap? = null
    private lateinit var mapView: MapView
    lateinit var totemzMapView: TotemzMapView
    lateinit var googleApiClient: GoogleApiClient
    var isMapReady = false
    val context: () -> Context by provider()
    // Map properties
    val DEFAULT_ZOOM = 13f
    lateinit var presenter: CameraPresenter
    val TAG = CameraFragment::class.java.simpleName

    companion object {
        fun newInstance(): MapFragment = MapFragment()
    }

    override fun getFragType(): FragmentTypes {
        return FragmentTypes.FRAG_MAP
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        context?.let {
            if (context is Activity) {
                //FIXME replace with coroutine that adds the marker when we get the location
//                when (o) {
//                    is User -> googleMap?.let {
//                        o.location?.let {
//                            googleMap?.createAndAddMarker(it.toLatLng(), R.mipmap.ic_totem)
//                        }
//                    }
//                    else -> {
//                    }
//                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_map, container, false)
        presenter = CameraPresenter()
        mapView = view.findViewById(R.id.map_totemz)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(this)
                .check()

        //init google API client
        googleApiClient = GoogleApiClient.Builder(context.invoke())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mapView.getMapAsync(this)

        return view
    }

    // Permission request callback
    @SuppressLint("MissingPermission")
    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        response?.let {
            if (response.permissionName == Manifest.permission.ACCESS_FINE_LOCATION) {
                googleMap?.let {
                    it.isMyLocationEnabled = true
                    it.uiSettings.isMyLocationButtonEnabled = true
                    getLocationAndAnimateMarker(LocationServices.FusedLocationApi.getLastLocation(googleApiClient))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            isMapReady = true
            Timber.i("Map Ready")
            it.loadMapStyle(context.invoke(), R.raw.google_map_style)
            this.googleMap = googleMap
            getLocationAndAnimateMarker(LocationServices.FusedLocationApi.getLastLocation(googleApiClient))
        }
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {
            googleMap?.clear()
            val lat = location.latitude
            val lng = location.longitude
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), DEFAULT_ZOOM))
            googleMap?.createAndAddMarker(LatLng(lat, lng), R.mipmap.ic_totem)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(connectionHint: Bundle?) {
        getLocationAndAnimateMarker(LocationServices.FusedLocationApi.getLastLocation(googleApiClient))
        Timber.d("API CONNECTED")
    }

    override fun onCameraMove() {
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    private fun getLocationAndAnimateMarker(location: Location?) {
        location?.let {
            val latLng = LatLng(location.latitude, location.longitude)
            googleMap?.createAndAddMarker(latLng, R.mipmap.ic_totem)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
            //FIXME replace with coorutines channel for locatrion sending
//            val subInterval = Observable.interval(6, TimeUnit.SECONDS)
//                    .subscribeOn(Schedulers.computation())
//                    .observeOn(AndroidSchedulers.mainThread()).subscribe {
//                        rxBus.invoke().send(it)
//                    }
            EventBus().send(it)
        }
    }

    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
    }

    override fun getPresenter(): BasePresenter<*> {
        return presenter
    }
    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}
