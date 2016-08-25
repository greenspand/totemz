package ro.cluj.totemz

import android.os.Bundle
import android.support.annotation.StringRes
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @StringRes
    override fun getActivityTitle(): Int {
        return R.string.app_name
    }

    override fun getRootLayout(): View {
        return container_totem
    }

}
