package ro.cluj.totemz.screens.user

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import ro.cluj.totemz.BasePresenter
import ro.cluj.totemz.realm.UserInfoRealm
import ro.cluj.totemz.utils.save

/**
 * Created by sorin on 7/12/16.
 */
class PresenterUserLogin : BasePresenter<ViewUserLogin>() {

    fun saveToUserRealm(signInAccount: GoogleSignInAccount?) {
        val realmUserInfo = UserInfoRealm()
        realmUserInfo.email = signInAccount?.email
        realmUserInfo.displayName = signInAccount?.displayName
        realmUserInfo.imageUrl = signInAccount?.photoUrl.toString()
        realmUserInfo.userID = signInAccount?.id
        realmUserInfo.save()
        view.showUserSavedToRealm()
    }
}
