package ro.cluj.totemz.mqtt

import io.reactivex.Observable
import io.reactivex.processors.PublishProcessor
import org.eclipse.paho.client.mqttv3.MqttMessage

object MqttEventBus {

    private val busMqttMsg by lazy { PublishProcessor.create<Pair<String, MqttMessage>>() }
    fun publishMqttMsg(mqttMsg: Pair<String, MqttMessage>) = busMqttMsg.onNext(mqttMsg)
    fun toObservableMqttMsg(): Observable<Pair<String, MqttMessage>> = busMqttMsg.toObservable()
    fun hasObserversMqttMsg(): Boolean = busMqttMsg.hasSubscribers()

    private val busMqttConnState by lazy { PublishProcessor.create<Boolean>() }
    fun publishMqttConnectionState(mqttMsg: Boolean) = busMqttConnState.onNext(mqttMsg)
    fun toObservableConnState(): Observable<Boolean> = busMqttConnState.toObservable()
    fun hasObserversConnState(): Boolean = busMqttConnState.hasSubscribers()
}