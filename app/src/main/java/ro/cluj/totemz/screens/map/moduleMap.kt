package ro.cluj.totemz.screens.map

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance

val mapModule = Kodein.Module {
    bind<PresenterMap>() with instance(PresenterMap())
}