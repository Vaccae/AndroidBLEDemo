package vac.test.bluetoothbledemo.State

import vac.test.bluetoothbledemo.bean.CBleDevice

sealed class BleDeviceState {

    object Idle : BleDeviceState()
    object Loading : BleDeviceState()
    data class BleDevices(val devices: MutableList<CBleDevice>) : BleDeviceState()
    data class Error(val error: String?) : BleDeviceState()
}