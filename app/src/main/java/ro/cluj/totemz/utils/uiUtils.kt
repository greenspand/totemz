package ro.cluj.totemz.utils

import android.app.Activity
import android.app.Fragment
import android.support.annotation.IdRes
import android.view.View

/**
 * Binds a view called from an Activity.
 * @param res the id of the View
 */
fun <T : View> Activity.bind(@IdRes res: Int): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(res) }

fun <T : View> Fragment.bind(root: View, @IdRes res: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { root.findViewById(res) as T }
}

/**
 * Binds into a custom View.
 * @param res the id of the View
 */
fun <T : View> View.bind(@IdRes res: Int): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(res) }