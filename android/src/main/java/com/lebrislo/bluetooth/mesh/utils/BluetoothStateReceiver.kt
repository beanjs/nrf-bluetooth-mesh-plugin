package com.lebrislo.bluetooth.mesh.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lebrislo.bluetooth.mesh.plugin.PluginCallManager

class BluetoothStateReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        when (state) {
            BluetoothAdapter.STATE_ON -> {
                PluginCallManager.getInstance().notifyAdapter(true)
            }

            BluetoothAdapter.STATE_OFF -> {
                PluginCallManager.getInstance().notifyAdapter(false)
            }
        }

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                PluginCallManager.getInstance().notifyConnection(true)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                PluginCallManager.getInstance().notifyConnection(false)
            }
        }
    }
}