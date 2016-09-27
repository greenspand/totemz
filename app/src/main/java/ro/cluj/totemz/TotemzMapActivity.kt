package ro.cluj.totemz

import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.activity_main.*
import ro.cluj.totemz.map.TotemzMapView
import ro.cluj.totemz.utils.withPermissionGranted

class TotemzMapActivity : BaseActivity(), TotemzMapView, OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab.setOnClickListener {

        }

        withPermissionGranted(android.Manifest.permission.ACCESS_FINE_LOCATION) {
            map.isMyLocationEnabled = true
        }
    }

    @StringRes
    override fun getActivityTitle(): Int {
        return R.string.app_name
    }

    override fun getRootLayout(): View {
        return container_totem
    }

}
