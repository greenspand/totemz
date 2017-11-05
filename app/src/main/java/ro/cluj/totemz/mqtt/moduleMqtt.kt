package ro.cluj.totemz.mqtt

/* ktlint-disable no-wildcard-imports */

import com.github.salomonbrys.kodein.*
import ro.cluj.totemz.utils.RxBus

val mqttModule = Kodein.Module {
    bind<MqttManager>() with provider { MqttManager(instance()) }
    bind<MqttEventBus>() with singleton { MqttEventBus }
    bind<RxBus>() with singleton { RxBus }
    bind<MQTTPresenter>() with provider { MQTTPresenter(instance(), instance()) }
}