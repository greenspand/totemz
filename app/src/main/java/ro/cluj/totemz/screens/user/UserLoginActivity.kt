package ro.cluj.totemz.screens.user

import android.app.Activity
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
import com.google.firebase.auth.*
import com.greenspand.kotlin_ext.snack
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import io.reactivex.disposables.Disposable
import io.reactivex.processors.BehaviorProcessor
import io.realm.SyncCredentials
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_user_login.*
import org.jetbrains.anko.intentFor
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.R
import ro.cluj.totemz.TotemzApp
import ro.cluj.totemz.mqtt.MQTTService
import timber.log.Timber
import java.util.*


/**
 * Created by sorin on 04.03.17.
 */
class UserLoginActivity : BaseActivity(), ViewUserLogin, GoogleApiClient.OnConnectionFailedListener, FacebookCallback<LoginResult> {

    private lateinit var callbackManager: CallbackManager
    private lateinit var gApiClient: GoogleApiClient
    private lateinit var gso: GoogleSignInOptions
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private var isLoggedIn = false
    private val RC_SIGN_IN = 78

    private val behaviourGoogleAccount: BehaviorProcessor<GoogleSignInAccount> = BehaviorProcessor.create()
    private lateinit var disposableGoogleAccount: Disposable

    val firebaseAuth: FirebaseAuth by instance()
    val presenter: PresenterUserLogin by instance()

    @StringRes
    override fun getActivityTitle(): Int {
        return 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)
        presenter.attachView(this)
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build()

        gApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                isLoggedIn = false
                // User is signed out
                Timber.i("onAuthStateChanged:signed_out")
            }
        }

        disposableGoogleAccount = behaviourGoogleAccount.subscribe {
            presenter.saveUserInfoToRealm(it)
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


        /**Twitter login Setup*/
        btnTwitterLogin.callback = object : Callback<TwitterSession>() {

            override fun success(result: Result<TwitterSession>) {
                firebaseAuthWithTwitter(result.data)
            }

            override fun failure(exception: TwitterException) {
                Timber.e(exception)
            }
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
        firebaseAuth.addAuthStateListener(authStateListener)

    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposableGoogleAccount.dispose()
        presenter.detachView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Twitter login callback
        btnTwitterLogin.onActivityResult(requestCode, resultCode, data)

        // Facebook Login callback
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) return

        //Google login callback
        when (requestCode) {
            RC_SIGN_IN -> Auth.GoogleSignInApi.getSignInResultFromIntent(data).handleLoginResult()
        }
    }


    private fun GoogleSignInResult.handleLoginResult() {
        if (this.isSuccess) {
            val signInAccount = this.signInAccount
            behaviourGoogleAccount.onNext(signInAccount)
        } else {
            snack(container_user_login, "User authentication failed !!!")
        }
    }


    private fun firebaseAuthWithFacebook(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseSignIn(credential)
        //TODO move this inside a realm sync service, we should start the service when user logged in
//        val credentials = SyncCredentials.facebook(token.token)
//        SyncUser.loginAsync(credentials, TotemzApp.AUTH_URL, this)
//        startService(intentFor<MQTTService>("facebookToken" to token.token))
    }

    private fun firebaseAuthWithTwitter(session: TwitterSession) {
        val credential = TwitterAuthProvider.getCredential(
                session.authToken.token,
                session.authToken.secret)
        firebaseSignIn(credential)
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        firebaseSignIn(credential)
    }

    fun firebaseSignIn(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this@UserLoginActivity) { task ->
                    Timber.i("signInWithCredential:onComplete:" + task.isSuccessful)
                    if (!task.isSuccessful) {
                        Timber.e("signInWithCredential", task.exception)
                        Toast.makeText(this@UserLoginActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Timber.e(result.errorMessage)
    }

    override fun showUserSavedToRealm() {
        Timber.i("USER WAS SAVED TO REALM")
    }

}