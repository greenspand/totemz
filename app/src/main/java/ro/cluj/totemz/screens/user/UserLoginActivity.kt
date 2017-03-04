package ro.cluj.totemz.screens.user

import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.R

/**
 * Created by sorin on 04.03.17.
 */
class UserLoginActivity : BaseActivity() {

    @StringRes
    override fun getActivityTitle(): Int {
        return 0
    }

    override fun getRootLayout(): View {
        return container_user_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnFacebookLogin.setOnClickListener { }
        btnGoogleLogin.setOnClickListener { }
    }

}