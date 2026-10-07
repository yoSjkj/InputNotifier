package com.hereng.inputnotifier

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat

class ConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val device = IntentCompat.getParcelableExtra(
            intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
        ) ?: return

        // 나중에 종류를 지정해도 바로 반영되도록 지정 여부와 상관없이 기록해 둔다
        val connections = ConnectionStore(context)
        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED ->
                connections.setConnected(device.address, device.displayName())
            BluetoothDevice.ACTION_ACL_DISCONNECTED ->
                connections.setDisconnected(device.address)
            else -> return
        }
        ConnectionNotifier.update(context)
    }
}
