package ro.cluj.totemz.models

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class StreetAddress(val street: String, val postalCode: String, val locality: String) : Parcelable
