package ro.cluj.totemz.utils

/* ktlint-disable no-wildcard-imports */

/**
 * Created by sorin on 12.10.16.
 */
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.animation.Interpolator
import io.reactivex.CompletableEmitter
import io.reactivex.CompletableOnSubscribe
import java.util.concurrent.atomic.AtomicInteger

class FadeInOutOnSubscribe(private val views: List<View>,
                           private val alpha: Float,
                           private val duration: Long,
                           private val interpolator: Interpolator)
    : CompletableOnSubscribe {

    private lateinit var numberOfAnimationsToRun: AtomicInteger
    override fun subscribe(e: CompletableEmitter?) {
        if (views.isEmpty()) {
            e?.onComplete()
            return
        }
        numberOfAnimationsToRun = AtomicInteger(views.size)
        // We need to run as much as animations as there are views.
        for (i in views.indices) {
            ViewCompat.animate(views[i])
                    .alpha(alpha)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .withEndAction {
                        // Once all animations are done, call onCompleted().
                        if (numberOfAnimationsToRun.decrementAndGet() == 0) {
                            e?.onComplete()
                        }
                    }
        }
    }
}