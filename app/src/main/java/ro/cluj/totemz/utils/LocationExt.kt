package com.moovel.drivenow.ui.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.moovel.drivenow.ui.entities.DrivenowMobilityPoint
import java.util.*

/**
 * Gets a complete address from anywhere where there is a provided context
 */
fun Context.getStreetAddress(mobilityPoint: DrivenowMobilityPoint?): StreetAddress? {
    try {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(mobilityPoint?.lat as Double, mobilityPoint?.lng as Double, 1)
        return addresses?.first()?.let { address ->
            StreetAddress(street = address.getAddressLine(0).toString(), postalCode = address.postalCode, locality = address.locality)
        }
    } catch (e:Exception) {
        return null
    }
}

data class StreetAddress(val street: String, val postalCode: String, val locality: String)


