package ro.cluj.totemz.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StreetAddress(val street: String, val postalCode: String, val locality: String) : Parcelable
