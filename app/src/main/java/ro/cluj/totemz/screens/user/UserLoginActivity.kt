package ro.cluj.totemz.screens.user

import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.github.salomonbrys.kodein.instance
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.greenspand.kotlin_ext.snack
import io.reactivex.disposables.Disposable
import io.reactivex.processors.BehaviorProcessor
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_user_login.*
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.R
import ro.cluj.totemz.realm.UserInfoRealm
import ro.cluj.totemz.utils.save
import timber.log.Timber
import java.util.*


/**
 * Created by sorin on 04.03.17.
 */
class UserLoginActivity : BaseActivity(), ViewUser, GoogleApiClient.OnConnectionFailedListener, FacebookCallback<LoginResult> {


    lateinit var callbackManager: CallbackManager
    lateinit var gApiClient: GoogleApiClient
    lateinit var gso: GoogleSignInOptions
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var isLoggedIn = false
    val behavGoogleAccnt: BehaviorProcessor<GoogleSignInAccount> = BehaviorProcessor.create()
    lateinit var dispGoogleAccnt: Disposable
    lateinit var presenter: PresenterUser
    val RC_SIGN_IN = 78

    val realm: Realm by instance()
    val firebaseAuth: FirebaseAuth by instance()

    @StringRes
    override fun getActivityTitle(): Int {
        return 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build()

        gApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        presenter = PresenterUser()

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                finish()
            } else {
                isLoggedIn = false
                // User is signed out
                Timber.i("onAuthStateChanged:signed_out")
            }
        }

        dispGoogleAccnt = behavGoogleAccnt.subscribe {
            firebaseAuthWithGoogle(it)
        }

        btnGoogleLogin.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(gApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


        /** Facebook login setup*/
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, this@UserLoginActivity)
        btnFacebookLogin.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this@UserLoginActivity, Arrays.asList("email", "public_profile", "user_friends"))
        }
    }

    override fun onCancel() {
        snack(container_user_login, "Facebook login cancelled")
    }

    override fun onSuccess(result: LoginResult) {
        firebaseAuthWithFacebook(result.accessToken)
    }

    override fun onError(error: FacebookException?) {
        Timber.e(error)
    }

    override fun onStart() {
        super.onStart()
        authStateListener?.let {
            firebaseAuth.addAuthStateListener(it)
        }
    }

    override fun onStop() {
        super.onStop()
        authStateListener?.let {
            firebaseAuth.removeAuthStateListener(it)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return
        }

        when (requestCode) {
            RC_SIGN_IN -> {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                result.handleLoginResult()
            }
        }
    }


    private fun GoogleSignInResult.handleLoginResult() {
        if (this.isSuccess) {
            val signInAccount = this.signInAccount
            val realmUserInfo = UserInfoRealm()
            realmUserInfo.email = signInAccount?.email
            realmUserInfo.displayName = signInAccount?.displayName
            realmUserInfo.imageUrl = signInAccount?.photoUrl.toString()
            realmUserInfo.userID = signInAccount?.id
            realmUserInfo.save()
            behavGoogleAccnt.onNext(signInAccount)
        } else {
            snack(container_user_login, "User authentication failed !!!")
        }
    }


    private fun firebaseAuthWithFacebook(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseSignIn(credential)
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        firebaseSignIn(credential)
    }

    fun firebaseSignIn(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Timber.i("signInWithCredential:onComplete:" + task.isSuccessful)
                    if (!task.isSuccessful) {
                        Timber.e("signInWithCredential", task.exception)
                        Toast.makeText(this@UserLoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    override fun onConnectionFailed(result: ConnectionResult) {

    }

    override fun onDestroy() {
        super.onDestroy()
        dispGoogleAccnt.dispose()
    }
}