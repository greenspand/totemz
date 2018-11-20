package ro.cluj.totemz.api

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import ro.cluj.totemz.model.TotemzMessage

const val MESSAGE_ENDPOINT = "v1/messages"

interface MessageApi {

    @GET("$MESSAGE_ENDPOINT/{id}")
    fun getMessage(id: String): Deferred<TotemzMessage>

    @GET(MESSAGE_ENDPOINT)
    fun getMessages(): Deferred<List<TotemzMessage>>
}