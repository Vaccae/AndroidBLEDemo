package vac.test.bluetoothbledemo.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.bean.CBleDevice
import vac.test.bluetoothbledemo.intent.ScanIntent
import vac.test.bluetoothbledemo.State.BleDeviceState

class ScanViewModel : ViewModel() {

    val actIntent = Channel<ScanIntent>(Channel.UNLIMITED)

    private val _bleDeviceState = MutableStateFlow<BleDeviceState>(BleDeviceState.Idle)
    val bleDeviceState: StateFlow<BleDeviceState>
        get() = _bleDeviceState

    private val mBleDeviceList= mutableListOf<CBleDevice>()

    init {
        initActIntent()
    }

    private fun initActIntent() {
        viewModelScope.launch {
            actIntent.consumeAsFlow().collect {
                when (it) {
                    is ScanIntent.BleDevice -> {
                        //蓝牙设备列表
                        val item = CBleDevice()
                        item.device = it.device
                        item.rssi = it.rssi
                        item.scanRecordBytes = it.scanRecordBytes
                        item.isConnectable = it.isConnectable
                        item.scanRecord = it.scanRecord

                        val idx = mBleDeviceList.indexOfFirst { idx ->
                            item.device == idx.device
                        }
//                        Log.i("pkg", "查找索引：${idx}")

                        if (idx < 0) {
                            mBleDeviceList.add(item)
                        }
                        Log.i("pkg", "集合数：${mBleDeviceList.size}")
                        //返回列表数据
                        _bleDeviceState.value = BleDeviceState.Loading
                        _bleDeviceState.value = BleDeviceState.BleDevices(mBleDeviceList)
//                        _bleDeviceState.emit(BleDeviceState.BleDevices(mBleDeviceList))
                    }

                    is ScanIntent.InitBleDevices -> {
                        _bleDeviceState.value = BleDeviceState.Loading
                    }
                }
            }
        }
    }
}