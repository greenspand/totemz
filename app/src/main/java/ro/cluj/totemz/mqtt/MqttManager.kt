package ro.cluj.totemz.mqtt

/* ktlint-disable no-wildcard-imports */
import android.app.Application
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import timber.log.Timber

class MqttManager(val application: Application) {
    private var established = false
    val BROKER_URL = "tcp://totemz.ddns.net:4000"
    private var topics: Array<String>? = null
    private var qos: IntArray? = null
    val clientID by lazy { Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID) }
    private val mqttClient: MqttAsyncClient by lazy { MqttAsyncClient(BROKER_URL, clientID, MemoryPersistence()) }

    private val options by lazy {
        MqttConnectOptions().apply {
            isCleanSession = true
        }
    }

    companion object {
        private const val RETRY_DELAY_AFTER_ERROR = 3_000L
    }

    fun connect(topics: Array<String>?, qos: IntArray?) {
        this@MqttManager.topics = topics
        this@MqttManager.qos = qos
        if (established) return
        established = true
        mqttClient.setCallback(object : MqttCallback {
            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                val msg = message.payload.toString(Charsets.UTF_8)
                Timber.w("Message arrived: $msg")
//                mqttEventBus.publishMqttMsg(topic to message)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit

            override fun connectionLost(cause: Throwable) {
//                mqttEventBus.publishMqttConnectionState(false)
                retry(RETRY_DELAY_AFTER_ERROR)
                Timber.w(cause, "MQTT connection lost")
            }
        })
        val connectAction: IMqttActionListener = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
//                mqttEventBus.publishMqttConnectionState(true)
                Timber.w("MQTT connected")
                mqttClient.subscribe(topics, qos, null, object : IMqttActionListener {

                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Timber.w("MQTT subscription successful")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                        Timber.e(exception, "MQTT could not subscribe")
                    }
                })
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
//                mqttEventBus.publishMqttConnectionState(false)
                retry(RETRY_DELAY_AFTER_ERROR)
                Timber.e(exception, "MQTT could not establish connection")
            }
        }
        mqttClient.connect(options, null, connectAction)
    }

    fun disconnect() {
        if (!established) return
        val disconnectAction: IMqttActionListener = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
//                mqttEventBus.publishMqttConnectionState(false)
                Timber.w("Mqtt Client disconnected")
                established = false
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                Timber.e(exception, "Could not disconnect MQTT client")
            }
        }

        try {
            mqttClient.disconnect(null, disconnectAction)
        } catch (cause: MqttException) {
            if (cause.reasonCode == MqttException.REASON_CODE_CLIENT_ALREADY_DISCONNECTED.toInt()) {
                established = false
                Timber.e(cause, "Client is already disconnected!")
            } else {
                Timber.e(cause, "Disconnection error: ")
            }
        }
    }

    private fun publishMsg(topic: String, msg: ByteArray) {
        mqttClient.publish(topic, MqttMessage(msg))
    }

    private fun retry(delay: Long? = null) {
        val operation = {
            val loggedIn = FirebaseAuth.getInstance().currentUser
            if (loggedIn != null) connect(topics, qos)
            else disconnect()
        }

        if (delay == null || delay <= 0) operation()
        else postDelayed(delay, operation)
    }

    private fun postDelayed(restartDelay: Long, run: () -> Unit) {
        if (restartDelay <= 0) {
            run()
        } else {
            launch {
                delay(restartDelay)
                run()
            }
        }
    }
}