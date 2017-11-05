package ro.cluj.totemz.screens

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import ro.cluj.totemz.screens.camera.CameraPresenter
import ro.cluj.totemz.screens.map.PresenterMap
import ro.cluj.totemz.screens.user.UserLoginPresenter
import ro.cluj.totemz.screens.user.UserPresenter

val screensModule = Kodein.Module {
    bind<UserLoginPresenter>() with instance(UserLoginPresenter())
    bind<CameraPresenter>() with instance(CameraPresenter())
    bind<UserPresenter>() with instance(UserPresenter())
    bind<PresenterMap>() with instance(PresenterMap())
    bind<TotemzBasePresenter>() with instance(TotemzBasePresenter())
}