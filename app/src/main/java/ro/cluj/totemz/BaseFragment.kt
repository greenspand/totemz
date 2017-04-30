package ro.cluj.totemz

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.appKodein
import com.google.firebase.auth.FirebaseAuth
import io.realm.Realm
import ro.cluj.totemz.model.FragmentTypes
import ro.cluj.totemz.screens.OnFragmentActionsListener
import ro.cluj.totemz.utils.RxBus

abstract class BaseFragment : Fragment(), LazyKodeinAware, OnFragmentActionsListener {

    /**
     * Init injector
     */
    override val kodein = LazyKodein(appKodein)

    lateinit var onFragmentActionsListener: OnFragmentActionsListener

    abstract fun getPresenter(): BasePresenter<*>
    abstract fun getFragType(): FragmentTypes

    val rxBus: () -> RxBus by provider()
    val realm: () -> Realm by provider()
    val firebaseAuth: () -> FirebaseAuth by provider()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        context?.let {
            if (context is Activity) {
                onFragmentActionsListener = context as OnFragmentActionsListener
            }
        }
    }

    override fun onNextFragment(fragType: FragmentTypes) {

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
        } else {

        }
    }

    override fun onDetach() {
        super.onDetach()
        getPresenter().detachView()
    }
}