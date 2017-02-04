package ro.cluj.totemz.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ro.cluj.totemz.BaseFragment
import ro.cluj.totemz.BasePresenter
import ro.cluj.totemz.model.FragmentTypes
import ro.cluj.totemz.R
import rx.subscriptions.CompositeSubscription

/**
 * Created by sorin on 11.10.16.
 *
 * Copyright (c) 2016 moovel GmbH<br>
 *
 * All rights reserved<br>
<p></p>
 */
class FragmentCamera : BaseFragment(), CameraView {



    private val subscriptions = CompositeSubscription()
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
        subscriptions.unsubscribe()
    }
}
