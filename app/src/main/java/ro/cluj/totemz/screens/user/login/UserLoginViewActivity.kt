package ro.cluj.totemz.screens.user.login

/* ktlint-disable no-wildcard-imports */

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
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
import com.google.firebase.auth.TwitterAuthProvider
import com.greenspand.kotlin_ext.editPrefs
import com.greenspand.kotlin_ext.setString
import com.greenspand.kotlin_ext.snack
import com.greenspand.kotlin_ext.toast
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.android.synthetic.main.activity_user_login.btnFacebookLogin
import kotlinx.android.synthetic.main.activity_user_login.btnGoogleLogin
import kotlinx.android.synthetic.main.activity_user_login.btnTwitterLogin
import kotlinx.android.synthetic.main.activity_user_login.container_user_login
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.R
import timber.log.Timber
import java.util.Arrays

/**
 * Created by sorin on 04.03.17.
 */
class UserLoginViewActivity : BaseActivity(),
  UserLoginView,
  GoogleApiClient.OnConnectionFailedListener,
  FacebookCallback<LoginResult> {
  private lateinit var callbackManager: CallbackManager
  private lateinit var gApiClient: GoogleApiClient
  private lateinit var gso: GoogleSignInOptions
  private lateinit var authStateListener: FirebaseAuth.AuthStateListener
  private var isLoggedIn = false
  private val RC_SIGN_IN = 78
  private val presenter: UserLoginPresenter by instance()
  private val channelGoogleSignIn by lazy { BroadcastChannel<GoogleSignInAccount>(1) }
  @StringRes override fun getActivityTitle() = 0

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

    launch {
      channelGoogleSignIn.openSubscription().consumeEach {
        presenter.saveUserInfoToRealm(it)
        firebaseAuthWithGoogle(it)
      }
    }

    btnGoogleLogin.setOnClickListener {
      val signInIntent = Auth.GoogleSignInApi.getSignInIntent(gApiClient)
      startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /** Facebook login setup*/
    callbackManager = CallbackManager.Factory.create()
    LoginManager.getInstance().registerCallback(callbackManager, this@UserLoginViewActivity)
    btnFacebookLogin.setOnClickListener {
      LoginManager.getInstance()
          .logInWithReadPermissions(this@UserLoginViewActivity, Arrays.asList("email", "public_profile", "user_friends"))
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
    firebaseAuth.invoke().addAuthStateListener(authStateListener)

  }

  override fun onStop() {
    super.onStop()
    firebaseAuth.invoke().removeAuthStateListener(authStateListener)
  }

  override fun onDestroy() {
    super.onDestroy()
    channelGoogleSignIn.close()
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
    if (isSuccess) {
      launch {
        signInAccount?.let { channelGoogleSignIn.send(it) }
      }
    } else {
      snack(container_user_login, "User authentication failed !!!")
    }
  }

  private fun firebaseAuthWithFacebook(token: AccessToken) {
    val credential = FacebookAuthProvider.getCredential(token.token)
    firebaseSignIn(credential)
    sharedPrefs.invoke().editPrefs {
      setString("FACEBOOK_TOKEN" to token.token)
    }
  }

  private fun firebaseAuthWithTwitter(session: TwitterSession) {
    val credential = TwitterAuthProvider.getCredential(
        session.authToken.token,
        session.authToken.secret)
    firebaseSignIn(credential)
  }

  private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
    firebaseSignIn(credential)
    sharedPrefs.invoke().editPrefs {
      setString("GOOGLE_TOKEN" to acct.idToken as String)
    }
  }

  private fun firebaseSignIn(credential: AuthCredential) {
    firebaseAuth.invoke().signInWithCredential(credential)
        .addOnCompleteListener(this@UserLoginViewActivity) { task ->
          Timber.i("signInWithCredential:onComplete: ${task.isSuccessful}")
          if (!task.isSuccessful) {
            Timber.e(task.exception)
            toast("Authentication failed.")
          }
        }
  }

  override fun onConnectionFailed(result: ConnectionResult) {
    Timber.e(result.errorMessage)
  }

  override fun showUserSavedToRealm() {
    Timber.i("Logged in User saved to realm")
  }
}