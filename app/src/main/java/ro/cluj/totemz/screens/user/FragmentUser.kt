package ro.cluj.totemz.screens.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_user_login.*
import ro.cluj.totemz.BaseFragment
import ro.cluj.totemz.BasePresenter
import ro.cluj.totemz.R
import ro.cluj.totemz.model.FragmentTypes
import ro.cluj.totemz.screens.camera.CameraPresenter
import ro.cluj.totemz.screens.camera.FragmentCamera
import ro.cluj.totemz.utils.fadeInOutAnimation
import timber.log.Timber

/**
 * Created by sorin on 11.10.16.
 *
 * Copyright (c) 2016 moovel GmbH<br>
 *
 * All rights reserved<br>
<p></p>
 */
class FragmentUser : BaseFragment(), ViewUser {

    private var isLoggedIn = false
    private val disposables = CompositeDisposable()
    lateinit var presenter: CameraPresenter
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    val TAG = FragmentCamera::class.java.simpleName

    companion object {
        fun newInstance(): FragmentUser {
            val fragment = FragmentUser()
            return fragment
        }
    }

    override fun getFragType(): FragmentTypes {
        return FragmentTypes.FRAG_USER
    }

    override fun getPresenter(): BasePresenter<*> {
        return presenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_user_profile, container, false)
        presenter = CameraPresenter()
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_logout.signOutListener()
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    fun FirebaseUser.setupLoggedIn() {
        isLoggedIn = true
        // User is signed in
        Timber.i("onAuthStateChanged:signed_in:" + this.uid)
        fadeInOutAnimation(mutableListOf(cont_logged_out), 0f, 500, AccelerateInterpolator())
                .mergeWith(fadeInOutAnimation(mutableListOf(btn_logout, cont_logged_in), 1f, 500, AccelerateInterpolator()))
                .subscribe {
                    tv_login_email.text = this.email
                    Picasso.with(activity)
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

}
