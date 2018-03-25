package ro.cluj.totemz.mqtt

/* ktlint-disable no-wildcard-imports */

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider

val mqttModule = Kodein.Module {
    bind<MqttManager>() with provider { MqttManager(instance()) }
    bind<MQTTPresenter>() with provider { MQTTPresenter(instance()) }
}