package ro.cluj.totemz.screens

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import ro.cluj.totemz.screens.camera.CameraPresenter
import ro.cluj.totemz.screens.map.MapPresenter
import ro.cluj.totemz.screens.user.UserPresenter
import ro.cluj.totemz.screens.user.login.UserLoginPresenter

val screensModule = Kodein.Module {
    bind<UserLoginPresenter>() with instance(UserLoginPresenter())
    bind<CameraPresenter>() with instance(CameraPresenter())
    bind<UserPresenter>() with instance(UserPresenter())
    bind<MapPresenter>() with instance(MapPresenter())
    bind<TotemzBasePresenter>() with instance(TotemzBasePresenter())
}