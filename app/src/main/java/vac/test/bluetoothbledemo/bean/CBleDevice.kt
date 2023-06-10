package vac.test.bluetoothbledemo.bean

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord

class CBleDevice {
    //蓝牙设备
    var device: BluetoothDevice? = null
    //信号值
    var rssi: Int = 0

    var scanRecordBytes: ByteArray? = null

    var isConnectable: Boolean = true

    var scanRecord: ScanRecord? = null
}