package ro.cluj.totemz.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TotemzMessage(
    val id: String,
    val groupId: String,
    val userId: String,
    val userName: String
)