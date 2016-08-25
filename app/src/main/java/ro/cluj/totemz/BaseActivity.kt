package ro.cluj.totemz


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.greenspand.kotlin_ext.snack

/**
 * Created by sorin on 8/21/16.
 */
 abstract class BaseActivity : AppCompatActivity() {

    abstract fun getActivityTitle(): Int

    abstract fun getRootLayout(): View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        snack(getRootLayout(), "Olaaa").show()
    }

    override fun setTitle(title: CharSequence) {
        super.setTitle(getActivityTitle())
    }

}
