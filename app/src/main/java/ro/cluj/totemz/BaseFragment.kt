package ro.cluj.totemz

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.provider
import com.google.firebase.auth.FirebaseAuth
import ro.cluj.totemz.models.FragmentTypes
import ro.cluj.totemz.screens.FragmentsActionsListener

abstract class BaseFragment : Fragment(), LazyKodeinAware, FragmentsActionsListener {

    /**
     * Init injector
     */
    override val kodein = LazyKodein(appKodein)

    private lateinit var fragmentActionsListener: FragmentsActionsListener

    abstract fun getPresenter(): BasePresenter<*>
    abstract fun getFragType(): FragmentTypes

    val firebaseAuth: () -> FirebaseAuth by provider()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        context?.let {
            if (context is Activity) {
                fragmentActionsListener = context as FragmentsActionsListener
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