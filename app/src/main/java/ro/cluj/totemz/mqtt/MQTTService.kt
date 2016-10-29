package ro.cluj.totemz.mqtt

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.MyLocation

val PARAM_LAT_LNG = "ro.cluj.totemz.push.LATLNG"

class MQTTService : Service(), MqttCallback {

    val TOPIC_MY_LOCATION = "/clientLocation"
    val TOPIC_FRIEND_LOCATION = "/friendLocation"

    val BROKER_URL = "tcp://greenspand.ddns.net:4000"

    var mqttClient: MqttClient? = null


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    private fun publishMsg(topic: String, msg: String) {
        mqttClient?.let {
            val message = MqttMessage(msg.toByteArray())
            it.publish(topic, message)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val android_id = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        try {
            mqttClient = MqttClient(BROKER_URL, android_id, MemoryPersistence())
            mqttClient?.setCallback(this)
            mqttClient?.connect()
            mqttClient?.subscribe(arrayOf(TOPIC_FRIEND_LOCATION))
        } catch (e: MqttException) {
            Toast.makeText(applicationContext, "Something went wrong!" + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun connectionLost(cause: Throwable) {
        Log.i("MSG", "connection lost")
    }


    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        Log.i("MSG", topic)
        when (topic) {
            TOPIC_FRIEND_LOCATION -> {
                val data = String(message.payload).split(",")
                EventBus.getDefault().post(FriendLocation(LatLng(data[0].toDouble(), data[1].toDouble())))
            }
        }
    }

    // Receive device user current location
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MyLocation) {
        publishMsg(TOPIC_MY_LOCATION, "${event.location.latitude} ${event.location.longitude}")
    }


    override fun deliveryComplete(token: IMqttDeliveryToken) {
        Log.i("MSG", "delivery copmplete")

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        try {
            mqttClient?.disconnect(0)
        } catch (e: MqttException) {
            Toast.makeText(applicationContext, "Something went wrong!" + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }
}
