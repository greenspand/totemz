package ro.cluj.totemz.screens

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance

/**
 * Created by sorin on 23.10.16.
 */
val mapModule = Kodein.Module {
    bind<TotemzBasePresenter>() with instance(TotemzBasePresenter())
}