package ro.cluj.totemz.utils

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttClientPersistence

/**
 * Created by sorin on 10.07.2017.
 */
inline fun createMqttClient(serverURI: String, clientId: String, persistence: MqttClientPersistence,
                            func: MqttAsyncClient.() -> Unit): MqttAsyncClient {
    val client = MqttAsyncClient(serverURI, clientId, persistence)
    client.func()
    return client
}
