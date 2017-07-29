package ro.cluj.totemz.mqtt

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.provider.Settings
import android.support.v4.content.LocalBroadcastManager
import com.github.salomonbrys.kodein.LazyKodein
import com.github.salomonbrys.kodein.LazyKodeinAware
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.provider
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.greenspand.kotlin_ext.toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import ro.cluj.totemz.model.FriendLocation
import ro.cluj.totemz.model.MyLocation
import ro.cluj.totemz.utils.RxBus
import ro.cluj.totemz.utils.createMqttClient
import timber.log.Timber


class TotemzMQTTService : Service(), MqttCallbackExtended, IMqttActionListener, ViewMQTT, LazyKodeinAware {

    override val kodein = LazyKodein(appKodein)
    val rxBus: () -> RxBus by provider()
    val realm: () -> Realm by provider()
    val firebaseDB: () -> FirebaseDatabase by provider()
    val TAG = TotemzMQTTService::class.java.simpleName
    var TOPIC_USER = "/user/"
    var TOPIC_FRIEND = "/friend/"
    val BROKER_URL = "tcp://totemz.ddns.net:4000"
    lateinit var presenter: PresenterMQTT
    var mqttClient: IMqttAsyncClient? = null
    val clientID by lazy { Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) }
    private val disposables by lazy { CompositeDisposable() }
    private val receiver by lazy { MqttBroadcastReceiver() }
    private val binder by lazy { LocalBinder() }
    private val firebaseDBRefFriendLocation: DatabaseReference by lazy { firebaseDB.invoke().getReference("FriendLocation") }

    companion object {

        const val ACTION_USER_LOCATION = "ro.cluj.totemz.mqtt.USER_LOCATION"
        const val ACTION_FRIEND_LOCATION = "ro.cluj.totemz.mqtt.ACTION_FRIEND_LOCATION"
        const val PARAM_FRIEND_LOCATION = "ro.cluj.totemz.mqtt.PARAM_FRIEND_LOCATION"
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {

        val service: TotemzMQTTService = this@TotemzMQTTService
    }

    override fun onBind(intent: Intent): IBinder? {
        val filter = IntentFilter(ACTION_USER_LOCATION)
        LocalBroadcastManager.getInstance(this@TotemzMQTTService).registerReceiver(receiver, filter)
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        return false
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseAnalytics.getInstance(this).setUserId(clientID)

        disposables.add(rxBus.invoke().toObservable()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { o ->
                    when (o) {
                        is MyLocation -> {
//                            val realmLocation = LocationRealm()
//                            realmLocation.clientID = clientID
//                            realmLocation.lat = o.location.latitude
//                            realmLocation.lon = o.location.longitude
//                            realmLocation.save()
                            publishMsg(TOPIC_USER, "$clientID:${o.location.latitude}:${o.location.longitude}".toByteArray())
                            //TODO FINALIZE PROTOBUF IMPLEMENTATION
//                            val userLocation = UserLocation.Builder().clientID(clientID).latitude(o.location.latitude).longitude(o.location.longitude).build()
//                            val payload = UserLocation.ADAPTER.encode(userLocation)
//                            publishMsg(TOPIC_USER, payload)
                        }
                    }
                })
        presenter = PresenterMQTT()
        presenter.attachView(this)

        mqttClient = createMqttClient(BROKER_URL, clientID, MemoryPersistence()) {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 3000
                keepAliveInterval = 10 * 60
            }
            val startMqtt = launch(CommonPool) {
                try {
                    connect(options).waitForCompletion(6000)
                    subscribe(TOPIC_FRIEND, 0).waitForCompletion()
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


    }

    private fun publishMsg(topic: String, msg: ByteArray) {
        mqttClient?.let {
            if (it.isConnected) {
                val message = MqttMessage(msg)
                it.publish(topic, message)
            }
        }
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        Timber.i("MQTT Connection Complete: $serverURI : reconnect: $reconnect")
        toast("MQTT Connection Complete: $serverURI : reconnect: $reconnect")
    }

    override fun connectionLost(cause: Throwable) {
        toast("Connection to Server lost")
    }

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        mqttClient?.subscribe(TOPIC_FRIEND, 0)
        toast("Connected")
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        Timber.e(exception, "MQTT Failed")
    }


    @Throws(Exception::class)
    override fun messageArrived(topic: String, message: MqttMessage) {
        when (topic) {
            TOPIC_FRIEND -> {
                //TODO FINALIZE PROTO-BUF IMPLEMENTATION
//                val location = UserLocation.ADAPTER.decode(message.payload)
//                if (location.clientID != clientID) {
//                    rxBus.send(FriendLocation(LatLng(location.latitude, location.longitude)))
//                }

                val msg = String(message.payload)
                if (msg.isNotEmpty()) {
                    val data = msg.split(":")
                    if (data[0] != clientID) {
                        val lat = data[1].toDouble()
                        val lng = data[2].toDouble()
                        val friendLoc = FriendLocation(LatLng(lat, lng))
                        val fbLoc = firebaseDBRefFriendLocation.child("FriendsLocations")
                        fbLoc.setValue(friendLoc)
                        fbLoc.push()
                        rxBus.invoke().send(friendLoc)

                        //Alternative to rxBus(Broadcast receiver)
                        val broadcastIntent = Intent(ACTION_FRIEND_LOCATION)
                        broadcastIntent.putExtra(PARAM_FRIEND_LOCATION, friendLoc)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
                    }
                }
            }
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken) {
        Timber.i("MSG", "delivery complete. Message id: ${token.message.id}")
    }

    override fun onDestroy() {
        disposables.dispose()
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
}
