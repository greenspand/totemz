package ro.cluj.totemz.mqtt

/* ktlint-disable no-wildcard-imports */
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.provider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.greenspand.kotlin_ext.toast
import io.realm.Realm
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import ro.cluj.totemz.models.TotemzMqttMessage
import ro.cluj.totemz.utils.createMqttClient
import timber.log.Timber

class MQTTService : Service(), MqttCallbackExtended, IMqttActionListener, MQTTView, LazyKodeinAware {
    override val kodein = LazyKodein(appKodein)
    private val realm: () -> Realm by provider()
    private val firebaseDB: () -> FirebaseDatabase by provider()
    private val presenter: () -> MQTTPresenter by provider()
    var TOPIC_USER = "/user/"
    var TOPIC_FRIEND = "/friend/"
    val BROKER_URL = "tcp://totemz.ddns.net:4000"
    var mqttClient: IMqttAsyncClient? = null
    val clientID by lazy { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) }
    private val firebaseDBRefFriendLocation: DatabaseReference by lazy {
        firebaseDB.invoke().getReference("FriendLocation")
    }
    private val sendMsg by lazy {
        sendMessage(TotemzMqttMessage.UserLocation("Sorin Test", Location("")))
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseAnalytics.getInstance(this).setUserId(clientID)

//        publishMsg(TOPIC_USER, "$clientID:${o.latitude}:${o.longitude}".toByteArray())
        //TODO FINALIZE PROTOBUF IMPLEMENTATION
//                            val userLocation = UserLocation.Builder().clientID(clientID).latitude(o.location.latitude).longitude(o.location.longitude).build()
//                            val payload = UserLocation.ADAPTER.encode(userLocation)
//                            publishMsg(TOPIC_USER, payload)
        sendMsg
        presenter.invoke().attachView(this)
        receiveMessage(sendMsg)
    }

    fun sendMessage(userLocation: TotemzMqttMessage.UserLocation) = produce<TotemzMqttMessage> {
        send(userLocation)
    }

    fun receiveMessage(channel: ReceiveChannel<TotemzMqttMessage>) = produce<TotemzMqttMessage> {
        for (msg in channel) {
            when (msg) {
                is TotemzMqttMessage.UserLocation -> Timber.w("UserLocation msg is: ${msg.name}")
                is TotemzMqttMessage.User -> Timber.w("User is: ${msg.name}")
                is TotemzMqttMessage.ChatMessage -> Timber.w("User is: ${msg.title}")
            }
        }
    }

    private fun publishMsg(topic: String, msg: ByteArray) {
        mqttClient?.let {
            if (it.isConnected) {
                val message = MqttMessage(msg)
                it.publish(topic, message)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mqttClient = createMqttClient(BROKER_URL, clientID, MemoryPersistence()) {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 3000
                keepAliveInterval = 10 * 60
            }
            val startMqtt = launch(CommonPool) {
                try {
                    val iMqttToken = connect(options)
                    iMqttToken.waitForCompletion(3500)
                    setCallback(this@MQTTService)
                    subscribe(TOPIC_FRIEND, 0)
                    iMqttToken.waitForCompletion(4000)
                    launch(UI) {
                        toast("Connected")
                    }
                } catch (e: Exception) {
                    launch(UI) {
                        toast(e.localizedMessage)
                    }
                }
            }
            startMqtt.start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun connectionLost(cause: Throwable) {
        toast("Connection to Server lost")
    }

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        Timber.i("Connected")
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        Timber.w("Reconnect state is: $reconnect Server uri: $serverURI")
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
    }

    override fun showMessage(topic: String, message: MqttMessage) {

    }

    override fun messageArrived(topic: String, message: MqttMessage) {
        when (topic) {
            TOPIC_FRIEND -> {
                //TODO FINALIZE PROTO-BUF IMPLEMENTATION
//                val location = UserLocation.ADAPTER.decode(message.payload)
//                if (location.clientID != clientID) {
//                    rxBus.send(FriendLocation(LatLng(location.latitude, location.longitude)))
//                }

//                val msg = String(message.payload)
//                if (msg.isNotEmpty()) {
//                    val data = msg.split(":")
//                    if (data[0] != clientID) {
//                        val lat = data[1].toDouble()
//                        val lng = data[2].toDouble()
//                        val friendLoc = FriendLocation(LatLng(lat, lng))
//                        val fbLoc = firebaseDBRefFriendLocation.child("FriendsLocations")
//                        fbLoc.setValue(friendLoc)
//                        fbLoc.push()
//                        rxBus.invoke().send(friendLoc)
//                    }
//                }
            }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        Log.i("MSG", "delivery complete")
    }

    override fun onDestroy() {
        try {
            mqttClient?.let {
                if (it.isConnected) it.disconnect()
                toast("Client disconnected")
            }
        } catch (e: MqttException) {
            Timber.e(e)
            toast("Something went wrong!" + e.message)
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
