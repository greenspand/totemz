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
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.jetbrains.anko.toast
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.MyLocation
import ro.cluj.totemz.utils.RxBus
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MQTTService() : Service(), MqttCallback, IMqttActionListener, ViewMQTT, KodeinInjected {


    override val injector = KodeinInjector()
    val rxBus: RxBus by instance()
    val TAG = MQTTService::class.java.simpleName
    var TOPIC_USER = "/user/"
    var TOPIC_FRIEND = "/friend/"
    val BROKER_URL = "tcp://greenspand.ddns.net:4000"
    lateinit var presenter: PresenterMQTT
    var client: MqttAndroidClient? = null
    var clientID: String? = null
    lateinit var sub: Subscription

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        inject(appKodein())

        clientID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) + "-android"

        sub = rxBus.toObservable().subscribeOn(Schedulers.computation()).observeOn(
                AndroidSchedulers.mainThread()).subscribe { o ->
            when (o) {
                is MyLocation -> {
                    publishMsg(TOPIC_USER, "$clientID:${o.location.latitude}:${o.location.longitude}")
                }
            }
        }
        presenter = PresenterMQTT()
        presenter.attachView(this)
    }

    private fun publishMsg(topic: String, msg: String) {
        client?.let {
            if (it.isConnected) {
                val message = MqttMessage(msg.toByteArray())
                it.publish(topic, message)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val clientID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) + "-android";
        val options = MqttConnectOptions()
        options.isCleanSession = true
        options.connectionTimeout = 3000
        options.keepAliveInterval = 10 * 60
        client = MqttAndroidClient(this, BROKER_URL, clientID)
        client?.connect(options, null, this)
        client?.setCallback(this)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun connectionLost(cause: Throwable) {
        toast("Connection to Server lost")
    }

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        client?.subscribe(TOPIC_FRIEND, 0)
        toast("Connected")
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
    }


    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        when (topic) {
            TOPIC_FRIEND -> {
                val msg = String(message.payload)
                if (msg.isNotEmpty()) {
                    val data = msg.split(":")
                    if (data[0] != clientID) {
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
            client?.let {
                if (it.isConnected) {
                    it.disconnectForcibly()
                    toast("Client disconnected")
                }
            }
        } catch (e: MqttException) {
            toast("Something went wrong!" + e.message)
            e.printStackTrace()
        }
    }
}
