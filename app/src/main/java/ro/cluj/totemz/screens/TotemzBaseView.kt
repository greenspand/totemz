package ro.cluj.totemz.screens

import ro.cluj.totemz.MvpBase
import ro.cluj.totemz.models.User

/**
 * Created by sorin on 7/12/16.
 */
interface TotemzBaseView : MvpBase.View {

    fun showFriendLocation(user: User)
}
