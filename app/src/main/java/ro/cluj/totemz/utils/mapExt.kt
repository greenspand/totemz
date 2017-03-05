package ro.cluj.totemz.utils

import android.content.Context
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.annotation.DrawableRes
import android.support.annotation.RawRes
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber
import java.util.*

/**
 * Gets a complete address from anywhere where there is a provided context
 */
fun Context.getStreetAddress(location: Location?): StreetAddress? {
    try {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(location?.latitude as Double, location.longitude, 1)
        return addresses?.first()?.let { address ->
            StreetAddress(street = address.getAddressLine(0).toString(), postalCode = address.postalCode, locality = address.locality)
        }
    } catch (e: Exception) {
        return null
    }
}

data class StreetAddress(val street: String, val postalCode: String, val locality: String)


fun GoogleMap.createAndAddMarker(latLng: LatLng, @DrawableRes markerResource: Int) {
    this.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(markerResource)))
}

fun GoogleMap.loadMapStyle(context: Context, @RawRes style: Int) {
    try {
        val success = setMapStyle(MapStyleOptions.loadRawResourceStyle(context, style))
        if (!success) {
            Timber.e("Map styling failed !!!!")
        }
    } catch (e: Resources.NotFoundException) {
        Timber.e("Map style not found !!!!", e)
    }

}