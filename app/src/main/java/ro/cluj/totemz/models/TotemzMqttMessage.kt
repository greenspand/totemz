package ro.cluj.totemz.models

import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by mihai on 3/25/2018.
 */
sealed class MqttMessage {
    @Parcelize
    data class User(val id: String, val name: String, val email, val location: Location) : MqttMessage(), Parcelable

    @Parcelize
    data class UserLocation(val name, val location: Location) : MqttMessage(), Parcelable

    @Parcelize
    data class ChatMessage(val id: String, val title: String, val content: String) : MqttMessage(), Parcelable
}