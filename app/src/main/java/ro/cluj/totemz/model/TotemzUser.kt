package ro.cluj.totemz.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TotemzUser(val id: String, val name: String, val email: String)
