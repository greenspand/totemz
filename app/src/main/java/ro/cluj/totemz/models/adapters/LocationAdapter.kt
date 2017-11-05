package ro.cluj.totemz.models.adapters

import android.location.Location
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class LocationAdapter {
    @FromJson
    fun locationFromJson(locationJSON: LocationJSON): Location {
        val location = Location("")
        location.longitude = locationJSON.longitude
        location.latitude = locationJSON.latitude
        return location
    }

    @ToJson
    fun locationToJson(location: Location): LocationJSON = LocationJSON(location.latitude, location.longitude)
}