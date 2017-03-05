package ro.cluj.totemz.screens.user

import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.greenspand.kotlin_ext.snack
import com.squareup.picasso.Picasso
import io.reactivex.disposables.Disposable
import io.reactivex.processors.BehaviorProcessor
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_user_login.*
import ro.cluj.totemz.BaseActivity
import ro.cluj.totemz.R
import ro.cluj.totemz.utils.fadeInOutAnimation
import timber.log.Timber


/**
 * Created by sorin on 04.03.17.
 */
class UserLoginActivity : BaseActivity(), ViewUser, GoogleApiClient.OnConnectionFailedListener {


    lateinit var presenter: PresenterUser
    lateinit var gApiClient: GoogleApiClient
    lateinit var gso: GoogleSignInOptions
    lateinit var mAuth: FirebaseAuth
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var isLoggedIn = false
    val behavGoogleAccnt: BehaviorProcessor<GoogleSignInAccount> = BehaviorProcessor.create()
    lateinit var dispGoogleAccnt: Disposable

    val RC_SIGN_IN = 78


    @StringRes
    override fun getActivityTitle(): Int {
        return 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_login)
        mAuth = FirebaseAuth.getInstance()

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

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.setupLoggedIn()
            } else {
                isLoggedIn = false
                // User is signed out
                Timber.i("onAuthStateChanged:signed_out")
                fadeInOutAnimation(mutableListOf(cont_logged_out), 1f, 500, AccelerateInterpolator())
                        .mergeWith(fadeInOutAnimation(mutableListOf(btn_logout, cont_logged_in), 0f, 500, AccelerateInterpolator()))
                        .subscribe {

                        }
            }
        }

        dispGoogleAccnt = behavGoogleAccnt.subscribe {
            firebaseAuthWithGoogle(it)
        }

        btnGoogleLogin.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(gApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        btnFacebookLogin.setOnClickListener {
        }

        btn_logout.signOutListener()
    }

    fun FirebaseUser.setupLoggedIn() {
        isLoggedIn = true
        // User is signed in
        Timber.i("onAuthStateChanged:signed_in:" + this.uid)
        fadeInOutAnimation(mutableListOf(cont_logged_out), 0f, 500, AccelerateInterpolator())
                .mergeWith(fadeInOutAnimation(mutableListOf(btn_logout, cont_logged_in), 1f, 500, AccelerateInterpolator()))
                .subscribe {
                    tv_login_email.text = this.email
                    Picasso.with(this@UserLoginActivity)
                            .load(this.photoUrl)
                            .error(R.drawable.vector_profle)
                            .transform(CropCircleTransformation())
                            .into(img_user)
                }
    }

    fun Button.signOutListener() {
        this.setOnClickListener {
            if (isLoggedIn) {
                FirebaseAuth.getInstance().signOut()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        mAuthListener?.let {
            mAuth.addAuthStateListener(it)
        }
    }

    public override fun onStop() {
        super.onStop()
        mAuthListener?.let {
            mAuth.removeAuthStateListener(it)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
            behavGoogleAccnt.onNext(signInAccount)
        } else {
            snack(container_user_login, "User authentication failed !!!")
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {

        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Timber.i("signInWithCredential:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
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