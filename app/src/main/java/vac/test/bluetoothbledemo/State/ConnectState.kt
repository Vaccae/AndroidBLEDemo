package vac.test.bluetoothbledemo.State

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import vac.test.bluetoothbledemo.bean.CBleDevice

sealed class ConnectState {
    object Idle : ConnectState()
    data class Connect(val gatt: BluetoothGatt) : ConnectState()
    data class Discovered(val gattservices: MutableList<BluetoothGattService>) : ConnectState()
    data class Info(val info:String?) :ConnectState()
    data class Error(val error: String?) : ConnectState()
}