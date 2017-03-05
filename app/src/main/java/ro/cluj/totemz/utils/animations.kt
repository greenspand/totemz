package ro.cluj.totemz.utils

import android.view.View
import android.view.animation.Interpolator
import io.reactivex.Completable

/**
 * Created by mihai on 3/5/2017.
 */

fun fadeInOutAnimation(items: MutableList<View>, alpha: Float, duration: Long, interpolator: Interpolator): Completable = Completable
        .create(FadeInOutOnSubscribe(items, alpha, duration, interpolator))