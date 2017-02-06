package com.greenspand.legoomo.utils

/**
 * Created by sorin on 12.10.16.
 */
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.animation.Interpolator
import io.reactivex.CompletableEmitter
import io.reactivex.CompletableOnSubscribe
import java.util.concurrent.atomic.AtomicInteger

class ExpandViewsOnSubscribe(private val views: List<View>,
                             private val scaleTo: Float,
                             private val duration: Long,
                             private val interpolator: Interpolator) : CompletableOnSubscribe {


    lateinit private var numberOfAnimationsToRun: AtomicInteger
    override fun subscribe(e: CompletableEmitter?) {
        if (views.isEmpty()) {
            e?.onComplete()
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
                            e?.onComplete()
                        }
                    }
        }
    }


}