package ro.cluj.totemz.mqtt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MqttBroadcastReceiver : BroadcastReceiver() {
    private var mReceiver: Receiver? = null

    override fun onReceive(context: Context, intent: Intent) {
        mReceiver?.onReceive(context, intent)
    }

    fun setReceiver(receiver: Receiver) {
        mReceiver = receiver
    }

    interface Receiver {
        fun onReceive(context: Context, intent: Intent)
    }
}