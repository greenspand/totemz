package ro.cluj.totemz.mqtt

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider

val mqttModule = Kodein.Module {
    bind<MqttManager>() with provider { TotemzMqttManager(instance(), instance()) }
    bind<MQTTPresenter>() with provider { MQTTPresenter(instance()) }
}