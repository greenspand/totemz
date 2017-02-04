package ro.cluj.totemz.map

import android.view.View
import android.view.animation.Interpolator
import ro.cluj.totemz.BasePresenter
import ro.cluj.totemz.utils.ExpandViewsOnSubscribe
import rx.Completable

/**
 * Created by sorin on 7/12/16.
 */
class TotemzBasePresenter : BasePresenter<TotemzBaseView>() {

    fun scaleAnimation(items: MutableList<View>, scale: Float, duration: Long, interpolator: Interpolator): Completable = Completable
            .create(ExpandViewsOnSubscribe(items, scale, duration, interpolator))
}
