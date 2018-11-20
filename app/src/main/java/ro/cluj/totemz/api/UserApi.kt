package ro.cluj.totemz.api

import kotlinx.coroutines.Deferred
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import ro.cluj.totemz.model.TotemzUser
import ro.cluj.totemz.model.UserRequest

const val USER_API = "v1/users"

interface UserApi {

    @GET(USER_API)
    fun getUsers(): Deferred<List<TotemzUser>>

    @GET("$USER_API/{id}")
    fun getUser(id: String): Deferred<TotemzUser>

    @POST("")
    fun addUser(userRequest: UserRequest)

    @PUT("")
    fun updateUser()

    @DELETE("/{id}")
    fun removeUser(id: String)

}