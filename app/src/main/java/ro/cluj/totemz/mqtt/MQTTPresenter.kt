package ro.cluj.totemz.mqtt

import ro.cluj.totemz.BasePresenter

/**
 * Created by sorin on 7/12/16.
 */
class MQTTPresenter(private val mqttManager: MqttManager)
    : BasePresenter<MQTTView>() {

    var TOPIC_USER = "/user/"
    var TOPIC_FRIEND = "/friend/"

    fun startMqttManager() {
        mqttManager.connect(arrayOf(TOPIC_FRIEND), intArrayOf(1))
//        mqttEventBus.toObservableMqttMsg()
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {
//                    val msg = it.second.payload.toString(Charsets.UTF_8)
//                    if (msg.isNotEmpty()) {
//                        when (it.first) {
//                            TOPIC_USER -> {
//
//                            }
//                            TOPIC_FRIEND -> {
//
//                            }
//                        }
//                    }
//                }
//        mqttEventBus.toObservableConnState()
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe {}

    }

    /**
     * Stops the MQTT service currently and if running.
     */
    fun stopMqttManager() {
        mqttManager.disconnect()
    }
}
