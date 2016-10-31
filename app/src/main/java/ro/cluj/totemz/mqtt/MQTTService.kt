package ro.cluj.totemz.mqtt

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.google.android.gms.maps.model.LatLng
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.jetbrains.anko.toast
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.MyLocation
import ro.cluj.totemz.utils.RxBus
import ro.cluj.totemz.utils.truncateDecimal
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MQTTService() : Service(), MqttCallback, KodeinInjected {

    override val injector = KodeinInjector()
    val rxBus: RxBus by instance()
     var android_id: String? = null
    var TOPIC_USER = "/user/"
    var TOPIC_FRIEND = "/friend/"
    val BROKER_URL = "tcp://greenspand.ddns.net:4000"

    var mqttClient: MqttClient? = null
    lateinit var sub: Subscription
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        inject(appKodein())
        android_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        sub = rxBus.toObservable().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe {
            o ->
            when (o) {
                is MyLocation ->{
                    android_id?.let {publishMsg(TOPIC_USER, "$android_id:${o.location.latitude}:${o.location.longitude}")}
                }
            }
        }
    }

    private fun publishMsg(topic: String, msg: String) {
        mqttClient?.let {
            if (it.isConnected) {
                val message = MqttMessage(msg.toByteArray())
                it.publish(topic, message)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            mqttClient = MqttClient(BROKER_URL, android_id, MemoryPersistence())
            mqttClient?.setCallback(this)
            mqttClient?.connect()
            mqttClient?.subscribe(arrayOf(TOPIC_FRIEND))
            toast("Client connected")
        } catch (e: MqttException) {
            toast("Something went wrong!" + e.message)
            e.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
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
                        if (data[0] != android_id) {
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
            mqttClient?.disconnect()
        } catch (e: MqttException) {
            toast("Something went wrong!" + e.message)
            e.printStackTrace()
        }
    }
}