package ro.cluj.totemz

interface MvpBase {
    interface View

    interface Presenter<V : View> {
        fun attachView(view: V)
        fun detachView()
    }
}
