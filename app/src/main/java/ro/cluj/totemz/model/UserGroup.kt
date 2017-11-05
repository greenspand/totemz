package ro.cluj.totemz.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by mihai on 7/2/2017.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class UserGroup(val name: String, val owner: User, val users: List<User>) : Parcelable
