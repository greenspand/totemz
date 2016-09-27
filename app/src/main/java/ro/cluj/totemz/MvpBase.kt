package com.moovel.drivenow.ui;

/**
 * Created by andre on 10/05/16.
 */
interface MvpBase {
    interface View

    interface Presenter<V : View> {
        fun attachView(view: V)
        fun detachView()
    }
}
