package ro.cluj.totemz.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage
import ro.cluj.totemz.MvpBase

/**
 * Created by sorin on 7/12/16.
 */
interface MQTTView : MvpBase.View {

    fun showMessage(topic: String, message: MqttMessage)
}
