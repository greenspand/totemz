package ro.cluj.totemz.screens.user

import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View
import kotlinx.android.synthetic.main.activity_user_login.*
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.R
import com.google.android.gms.auth.api.signin.GoogleSignInOptions



/**
 * Created by sorin on 04.03.17.
 */
class UserLoginActivity : BaseActivity(), ViewUser{

    lateinit var presenter: PresenterUser

    @StringRes
    override fun getActivityTitle(): Int {
        return 0
    }

    override fun getRootLayout(): View {
        return container_user_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        presenter  = PresenterUser()
        btnFacebookLogin.setOnClickListener {


        }
        btnGoogleLogin.setOnClickListener { }
    }

}