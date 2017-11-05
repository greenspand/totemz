package ro.cluj.totemz.models

import android.annotation.SuppressLint
import android.location.Location
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by mihai on 7/2/2017.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class User(val id: String? = null, val name: String? = null, val email: String? = null, val location: Location? = null) : Parcelable
