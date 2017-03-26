package ro.cluj.totemz.screens.camera

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.frag_totem_camera.*
import ro.cluj.totemz.BaseFragment
import ro.cluj.totemz.BasePresenter
import ro.cluj.totemz.R
import ro.cluj.totemz.model.FragmentTypes

/**
 * Created by sorin on 11.10.16.
 *
 * Copyright (c) 2016 moovel GmbH<br>
 *
 * All rights reserved<br>
<p></p>
 */
class FragmentCamera : BaseFragment(), CameraView {

    var CAMERA_REQUEST = 93


    private val subscriptions = CompositeDisposable()
    lateinit var presenter: CameraPresenter
    val TAG = FragmentCamera::class.java.simpleName

    companion object {
        fun newInstance(): FragmentCamera {
            val fragment = FragmentCamera()
            return fragment
        }
    }

    override fun getFragType(): FragmentTypes {
        return FragmentTypes.FRAG_CAM
    }

    override fun getPresenter(): BasePresenter<*> {
        return presenter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_totem_camera, container, false)
        presenter = CameraPresenter()
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
        }
    }

    override fun onDetach() {
        super.onDetach()
        subscriptions.clear()
    }
}
