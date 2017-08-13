package ro.cluj.totemz.screens.user

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance

/**
 * Created by sorin on 11.03.17.
 */
val userModule = Kodein.Module {
    bind<UserLoginPresenter>() with instance(UserLoginPresenter())
    bind<UserPresenter>() with instance(UserPresenter())
}