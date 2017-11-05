package ro.cluj.totemz.model

import android.annotation.SuppressLint
import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by mihai on 7/2/2017.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class User(val id: String, val name: String?, val email: String?, val location: Location?) : Parcelable
