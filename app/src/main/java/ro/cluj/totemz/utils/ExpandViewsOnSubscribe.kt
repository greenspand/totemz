package ro.cluj.totemz.utils

/**
 * Created by sorin on 12.10.16.
 */
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.animation.Interpolator
import rx.Completable
import java.util.concurrent.atomic.AtomicInteger

class ExpandViewsOnSubscribe(private val views: List<View>,
                             private val scaleTo: Float,
                             private val duration: Long,
                             private val interpolator: Interpolator) : Completable.CompletableOnSubscribe {


    lateinit private var numberOfAnimationsToRun: AtomicInteger

    override fun call(subscriber: Completable.CompletableSubscriber) {
        if (views.isEmpty()) {
            subscriber.onCompleted()
            return
        }
        numberOfAnimationsToRun = AtomicInteger(views.size)


        // We need to run as much as animations as there are views.
        for (i in views.indices) {
            ViewCompat.animate(views[i])
                    .scaleX(scaleTo)
                    .scaleY(scaleTo)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .withEndAction {
                        // Once all animations are done, call onCompleted().
                        if (numberOfAnimationsToRun.decrementAndGet() == 0) {
                            subscriber.onCompleted()
                        }
                    }
        }
    }



}