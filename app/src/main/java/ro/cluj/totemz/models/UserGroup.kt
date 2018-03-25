package ro.cluj.totemz.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by mihai on 7/2/2017.
 */
@Parcelize
data class UserGroup(val name: String? = null, val owner: User? = null, val users: List<User>? = null) : Parcelable
