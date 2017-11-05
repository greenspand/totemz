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

/**
 * @return - the bearing we need to go from this point to the specified desired point
 */
fun LatLng.initialBearingTo(to: LatLng): Float {
    val result = FloatArray(2)
    android.location.Location.distanceBetween(latitude, longitude, to.latitude, to.longitude, result)
    return result[1]
}

/**
 * Object useful for representing a point in the polyline
 */
data class PointOnPath(
        val start: LatLng,
        val startIndex: Int,
        val end: LatLng,
        val endIndex: Int,
        val snapped: LatLng,
        val distance: Double
)

/**
 * Utility method to convert android.Location to a LatLng representation
 */
fun android.location.Location.toLatLng() = LatLng(this.latitude, this.longitude)

/**
 * converts a path to a list of lat lng polyline.
 * @return List of Location
 */
fun String.polyline(): List<LatLng> {
    val len = this.length
    val path = ArrayList<LatLng>()
    var index = 0
    var lat = 0
    var lng = 0
    try {
        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = this[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            result = 1
            shift = 0
            do {
                b = this[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            path.add(LatLng(lat * 1e-5, lng * 1e-5))
        }
    } catch (e: Exception) {
        Timber.e("The current string is not an encoded polyline", e)
    }
    return path
}

/**
 * converts a path to a list of lat lng polyline.
 * @return List of LatLng
 */
fun String.polylineLatLng(): List<LatLng> {
    val len = this.length
    val path = ArrayList<LatLng>()
    var index = 0
    var lat = 0
    var lng = 0
    try {
        while (index < len) {
            var result = 1
            var shift = 0
            var b: Int
            do {
                b = this[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            result = 1
            shift = 0
            do {
                b = this[index++].toInt() - 63 - 1
                result += b shl shift
                shift += 5
            } while (b >= 0x1f)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1
            path.add(LatLng(lat * 1e-5, lng * 1e-5))
        }
    } catch (e: Exception) {
        Timber.e("The current string is not an encoded polyline", e)
    }
    return path
}

/**
 * Calculate the distance how long the polyline is
 */
fun List<LatLng>.distance(): Double = (1 until size).map { this[it - 1].distanceBetween(this[it]) }.sum()

/**
 * Function used to calculate the amount of degrees between this two parameter degrees
 */
fun degreesBetween(b1: Float, b2: Float): Float {
    val nb1 = normalizeBearing(b1)
    val nb2 = normalizeBearing(b2)

    val max = Math.max(nb1, nb2)
    val min = Math.min(nb1, nb2)

    val maxMinusMin = max - min
    val minMinusMax = normalizeBearing(min - max)

    return Math.min(maxMinusMin, minMinusMax)
}

/**
 * Alternative calculation for the distance between two point.
 * @return - the distance of the other point from this point.
 */
fun LatLng.distanceBetween(to: LatLng): Double {
    val result = FloatArray(1)
    android.location.Location.distanceBetween(latitude, longitude, to.latitude, to.longitude, result)
    return result[0].toDouble()
}

/**
 * Function to find the point where we would end if we head with the specified heading for the requested distance
 * @param distance -  the distance to move
 * @param bearing - the bearing we are heading to
 */
fun LatLng.destination(distance: Double, bearing: Double): LatLng {
    val (lat, lng) = this
    val latitude1 = Math.toRadians(lat)
    val longitude1 = Math.toRadians(lng)
    val bearing1 = Math.toRadians(bearing)

    val R = 6373_000 // meters

    val latitude2 = Math.asin(Math.sin(latitude1) * Math.cos(distance / R) +
            Math.cos(latitude1) * Math.sin(distance / R) * Math.cos(bearing1))
    val longitude2 = longitude1 + Math.atan2(Math.sin(bearing1) * Math.sin(distance / R) * Math.cos(latitude1),
            Math.cos(distance / R) - Math.sin(latitude1) * Math.sin(latitude2))

    // FIXME fix handle possible latitude must not be Nan exception
    return try {
        LatLng(Math.toDegrees(latitude2), Math.toDegrees(longitude2))
    } catch (e: Exception) {
        this
    }
}

private fun lineIntersects(l1Start: LatLng, l1End: LatLng, l2Start: LatLng, l2End: LatLng): LatLng? {
    val (line1StartY, line1StartX) = l1Start
    val (line1EndY, line1EndX) = l1End
    val (line2StartY, line2StartX) = l2Start
    val (line2EndY, line2EndX) = l2End
    val denominator = ((line2EndY - line2StartY) * (line1EndX - line1StartX)) - ((line2EndX - line2StartX) * (line1EndY - line1StartY))
    if (denominator == 0.0) return null
    var a = line1StartY - line2StartY
    var b = line1StartX - line2StartX
    val numerator1 = ((line2EndX - line2StartX) * a) - ((line2EndY - line2StartY) * b)
    val numerator2 = ((line1EndX - line1StartX) * a) - ((line1EndY - line1StartY) * b)
    a = numerator1 / denominator
    b = numerator2 / denominator

    val x = line1StartX + (a * (line1EndX - line1StartX))
    val y = line1StartY + (a * (line1EndY - line1StartY))

    val onLine1 = a > 0 && a < 1
    val onLine2 = b > 0 && b < 1

    return if (onLine1 && onLine2) {
        LatLng(y, x)
    } else {
        null
    }
}

private fun normalizeBearing(bearing: Float): Float = (360f + bearing) % 360

operator fun LatLng.component1(): Double = latitude
operator fun LatLng.component2(): Double = longitude