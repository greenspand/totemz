package ro.cluj.totemz


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.greenspand.kotlin_ext.snack

/**
 * Created by sorin on 8/21/16.
 */
abstract class BaseActivity : AppCompatActivity(), KodeinInjected {

    abstract fun getActivityTitle(): Int

    abstract fun getRootLayout(): View

    val log: Logger by instance()
    override val injector = KodeinInjector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appKodein())
        Log.i("Kodein", "=====================-BINDINGS-=====================")
        log.callback = {
            Log.i("RECEIVED", "received")
        }
    }

    override fun onResume() {
        super.onResume()
        snack(getRootLayout(), "Olaaa").show()
    }

    override fun setTitle(title: CharSequence) {
        super.setTitle(getActivityTitle())
    }

}
