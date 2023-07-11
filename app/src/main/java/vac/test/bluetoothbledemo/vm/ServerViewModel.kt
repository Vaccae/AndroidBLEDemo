package vac.test.bluetoothbledemo.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.bean.CPhone
import vac.test.bluetoothbledemo.State.PhoneState
import vac.test.bluetoothbledemo.intent.ServerIntent
import vac.test.bluetoothbledemo.State.ServerState

class ServerViewModel : ViewModel() {

    val serverIntent = Channel<ServerIntent>(Channel.UNLIMITED)

    private val _serverState = MutableStateFlow<ServerState>(ServerState.Info(""))
    val serverState: StateFlow<ServerState>
        get() = _serverState

    private val _phone = MutableStateFlow<PhoneState>(PhoneState.Idle)
    val phone : StateFlow<PhoneState> get() = _phone


    init {
        initServerIntent()

        getDeviceInfo()
    }

    private fun getDeviceInfo(){
        viewModelScope.launch {
            val device = CPhone()
            device.name = android.os.Build.DEVICE
            device.modelname = android.os.Build.MODEL
            device.version = android.os.Build.VERSION.RELEASE
            device.sdkversion = android.os.Build.VERSION.SDK
            device.brand = android.os.Build.BRAND
            device.manufacturer = android.os.Build.MANUFACTURER

            _phone.value = PhoneState.Phone(device)
        }
    }

    private fun initServerIntent() {
        viewModelScope.launch {
            serverIntent.consumeAsFlow().collect {
                when (it) {
                    is ServerIntent.Info -> {
                        _serverState.emit(ServerState.Info(it.msg))
                        _phone.value = PhoneState.Error(it.msg)
                        delay(1000)
                        getDeviceInfo()
                    }
                    is ServerIntent.Error -> {
                        _serverState.emit(ServerState.Error(it.msg))
                    }
                    is ServerIntent.RecvByteArray -> {
                        it.bytes?.let {
                            //开始组装数据
                        }
                    }
                }
            }
        }
    }
}