package vac.test.bluetoothbledemo.vm

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.State.ConnectState
import vac.test.bluetoothbledemo.intent.ConnectIntent
import vac.test.bluetoothbledemo.repository.BlueToothBLEUtil

class ConnectViewModel : ViewModel() {

    val connectIntent = Channel<ConnectIntent>(Channel.UNLIMITED)

    private val _connectState = MutableStateFlow<ConnectState>(ConnectState.Idle)
    val connectState: StateFlow<ConnectState>
        get() = _connectState

    //连接状态
    var isConnect = false
    var curGatt: BluetoothGatt? = null
    var gattservices = mutableListOf<BluetoothGattService>()

    init {
        initConnectIntent()
    }

    private fun initConnectIntent() {
        viewModelScope.launch {
            connectIntent.consumeAsFlow().collect {
                when (it) {
                    is ConnectIntent.Connect -> {
                        Log.i("pkg", "Connect ${it.gatt.device}")
                        curGatt = it.gatt
                        connectDevice(curGatt!!)
                        isConnect = true

                        //开始发现服务
                        BlueToothBLEUtil.discoverServices()
                    }

                    is ConnectIntent.Discovered -> {
                        gattservices = it.gattservices.toMutableList()
                        _connectState.value = ConnectState.Discovered(gattservices)
                    }

                    is ConnectIntent.DisConnect -> {
                        isConnect = false
                        _connectState.value = ConnectState.Idle
                    }

                    is ConnectIntent.WriteCharacteristic ->{
//                        val byteArray = it.str.toByteArray(charset = Charsets.UTF_8)
//                        BlueToothBLEUtil.writeCharacteristic(it.characteristic, byteArray)
                        launch {
                            val byteArray = it.str.toByteArray(charset = Charsets.UTF_8)
                            BlueToothBLEUtil.writeCharacteristicSplit(it.characteristic, byteArray)
                        }
                    }

                    is ConnectIntent.ReadCharacteristic ->{
                        val byteArray = BlueToothBLEUtil.readCharacteristic(it.characteristic)
                        byteArray?.let { bytes->
                            _connectState.value = ConnectState.Info(String(bytes))
                        }
                    }

                    is ConnectIntent.CharacteristicNotify ->{
                        _connectState.value = ConnectState.Info(it.str)
                    }

                    is ConnectIntent.Error ->{
                        _connectState.value = ConnectState.Error(it.msg)
                    }
                }
            }
        }
    }

    private fun connectDevice(gatt: BluetoothGatt) {
        _connectState.value = ConnectState.Connect(gatt)
    }
}