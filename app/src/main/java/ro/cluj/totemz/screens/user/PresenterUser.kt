package ro.cluj.totemz.screens.user

import android.view.View
import android.view.animation.Interpolator
import io.reactivex.Completable
import ro.cluj.totemz.BasePresenter
import ro.cluj.totemz.utils.ExpandViewsOnSubscribe

/**
 * Created by sorin on 7/12/16.
 */
class PresenterUser : BasePresenter<ViewUser>() {

    fun fadeInOutAnimation(items: MutableList<View>, alpha: Float, duration: Long, interpolator: Interpolator): Completable = Completable
            .create(ExpandViewsOnSubscribe(items, alpha, duration, interpolator))
}
