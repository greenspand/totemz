package ro.cluj.totemz.mqtt

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.google.android.gms.maps.model.LatLng
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.jetbrains.anko.toast
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.MyLocation
import ro.cluj.totemz.model.UserInfo
import ro.cluj.totemz.utils.RxBus
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MQTTService() : Service(), MqttCallback, IMqttActionListener, KodeinInjected {


    override val injector = KodeinInjector()
    val rxBus: RxBus by instance()
    val userInfo: UserInfo by instance()
    var TOPIC_USER = "/user/"
    var TOPIC_FRIEND = "/friend/"
    val BROKER_URL = "tcp://greenspand.ddns.net:4000"

    var mqttClient: MqttAndroidClient? = null

    lateinit var sub: Subscription

    private val binder = LocalBinder()

    //BInder implementation details
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        val service: MQTTService
            get() = this@MQTTService
    }
    override fun onCreate() {
        super.onCreate()
        inject(appKodein())
        sub = rxBus.toObservable().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            o ->
            when (o) {
                is MyLocation -> {
                    publishMsg(TOPIC_USER, "${userInfo.id}:${o.location.latitude}:${o.location.longitude}")
                }
            }
        }
    }

    private fun publishMsg(topic: String, msg: String) {
        mqttClient?.let {
            if (it.isConnected) {
                val message = MqttMessage(msg.toByteArray())
                message.qos = 2
                mqttClient?.publish(topic, message)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            mqttClient = MqttAndroidClient(this@MQTTService, BROKER_URL, userInfo.id, MemoryPersistence())
            mqttClient?.setCallback(this@MQTTService)
            mqttClient?.connect()
        } catch(e: MqttException) {
            toast("Error" + e.message)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        Log.i("MQTT", "Client connected")
        Log.i("MQTT", "Topics=" + asyncActionToken?.topics)
        mqttClient?.subscribe(TOPIC_FRIEND, 2)
        toast("Client connected")
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        toast("Couldn't connect to server")
    }

    override fun connectionLost(cause: Throwable) {
        toast("Connection to Server lost")
    }


    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        when (topic) {
            TOPIC_FRIEND -> {
                val msg = String(message.payload)
                if (msg.isNotEmpty()) {
                    val data = msg.split(":")
                    if (data[0] != userInfo.id) {
                        val lat = data[1].toDouble()
                        val lng = data[2].toDouble()
                        rxBus.send(FriendLocation(LatLng(lat, lng)))
                    }
                }
            }
        }
    }


    override fun deliveryComplete(token: IMqttDeliveryToken) {
        Log.i("MSG", "delivery copmplete")

    }

    override fun onDestroy() {
        sub.unsubscribe()
        try {
            mqttClient?.let { if (it.isConnected) it.disconnectForcibly() }
        } catch (e: MqttException) {
            toast("Something went wrong!" + e.message)
            e.printStackTrace()
        }
    }
}
