package ro.cluj.totemz.mqtt

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.provider
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import ro.cluj.totemz.model.UserInfo

/**
 * Created by sorin on 01.11.16.
 */
class MqttModule(val userInfo: UserInfo) {

}

val mqttModule = Kodein.Module {
    bind<MqttModule>() with provider{MqttModule(instance())}
}