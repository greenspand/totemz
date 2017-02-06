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
import com.google.firebase.analytics.FirebaseAnalytics
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.MyLocation
import ro.cluj.totemz.utils.RxBus
import timber.log.Timber


class MQTTService : Service(), MqttCallback, IMqttActionListener, ViewMQTT, KodeinInjected {

    override val injector = KodeinInjector()
    val rxBus: RxBus by instance()
    val TAG = MQTTService::class.java.simpleName
    var TOPIC_USER = "/user/"
    var TOPIC_FRIEND = "/friend/"
    val BROKER_URL = "tcp://totemz.ddns.net:4000"
    val ANDROID_OS = "-android"
    lateinit var presenter: PresenterMQTT
    lateinit var mqttClient: MqttClient
    var clientID: String? = null
    private val disposables = CompositeDisposable()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        inject(appKodein())

        clientID = "${Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)}$ANDROID_OS"
        FirebaseAnalytics.getInstance(this).setUserId(clientID)

        disposables.add(rxBus.toObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { o ->
                    when (o) {
                        is MyLocation -> {
                            publishMsg(TOPIC_USER, "$clientID:${o.location.latitude}:${o.location.longitude}")
                        }
                    }
                })
        presenter = PresenterMQTT()
        presenter.attachView(this)
    }

    private fun publishMsg(topic: String, msg: String) {
        mqttClient.let {
            if (it.isConnected) {
                val message = MqttMessage(msg.toByteArray())
                it.publish(topic, message)
            }
        }
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val clientID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) + "-android"
//        val options = MqttConnectOptions()
//        options.isCleanSession = true
//        options.connectionTimeout = 3000
//        options.keepAliveInterval = 10 * 60
        doAsync {
            try {
                mqttClient = MqttClient(BROKER_URL, clientID, MemoryPersistence())
                mqttClient.setCallback(this@MQTTService)
                mqttClient.connect()
                mqttClient.subscribe(arrayOf(TOPIC_FRIEND))
                runOnUiThread {
                    toast("Client connected")
                }
            } catch(e: MqttException) {
                Timber.e(e)
                runOnUiThread {
                    toast("Error" + e.message)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun connectionLost(cause: Throwable) {
        toast("Connection to Server lost")
    }

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        mqttClient.subscribe(TOPIC_FRIEND, 0)
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
        Log.i("MSG", "delivery complete")
    }

    override fun onDestroy() {
        disposables.dispose()
        try {
            mqttClient.disconnect()
            toast("Client disconnected")
        } catch (e: MqttException) {
            Timber.e(e)
            toast("Something went wrong!" + e.message)
            e.printStackTrace()
        }
    }
}
