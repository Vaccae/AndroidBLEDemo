package vac.test.bluetoothbledemo.intent

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord

sealed class ScanIntent {
    //初始化蓝牙列表
    object InitBleDevices : ScanIntent()
    //蓝牙设备
    data class BleDevice(var device: BluetoothDevice, var rssi: Int,
                         var scanRecordBytes: ByteArray, var isConnectable:Boolean = true,
                         var scanRecord: ScanRecord? = null) : ScanIntent()
}