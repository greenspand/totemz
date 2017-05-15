package ro.cluj.totemz.utils

import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor

/**
 * courtesy: https://gist.github.com/benjchristensen/04eef9ca0851f3a5d7bf
 */
object RxBus {

    private val bus = PublishProcessor.create<Any>()

    fun send(o: Any) {
        bus.onNext(o)
    }

    fun toObservable(): Observable<Any> {
        return bus.toObservable()
    }

    fun hasObservers(): Boolean {
        return bus.hasSubscribers()
    }
}