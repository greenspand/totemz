package ro.cluj.totemz.screens.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
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


    override fun onDetach() {
        super.onDetach()
        subscriptions.clear()
    }
}
