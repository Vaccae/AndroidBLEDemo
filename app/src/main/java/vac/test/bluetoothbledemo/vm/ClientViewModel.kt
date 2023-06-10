package vac.test.bluetoothbledemo.vm

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.State.ClientState
import vac.test.bluetoothbledemo.State.ConnectState
import vac.test.bluetoothbledemo.bean.CBleDevice
import vac.test.bluetoothbledemo.intent.ClientIntent

class ClientViewModel : ViewModel() {

    val clientIntent = Channel<ClientIntent>(Channel.UNLIMITED)

    private val _clientState = MutableStateFlow<ClientState>(ClientState.ScanMode)
    val clientState: StateFlow<ClientState>
        get() = _clientState

    var curBleDevice :CBleDevice = CBleDevice()

    init {
        initClientViewModel()
    }


    private fun initClientViewModel(){
        viewModelScope.launch {
            clientIntent.consumeAsFlow()
                .collect{
                Log.i("pkg", "clientintent ${it}")
                when(it){
                    is ClientIntent.ScanMode ->{
                        _clientState.value = ClientState.ScanMode
                    }
                    is ClientIntent.ConnectMode ->{
                        _clientState.value = ClientState.ConnectMode
                    }
                    is ClientIntent.Connect ->{
                        _clientState.value = ClientState.ConnectMode
                        delay(200)
                        curBleDevice = it.dev
                        _clientState.value = ClientState.Connect(it.dev)
                    }
                    is ClientIntent.DisConnect ->{
                        _clientState.value = ClientState.DisConnect
                    }
                    is ClientIntent.Error -> {
                        _clientState.value = ClientState.Error(it.msg)
                    }
                }
            }
        }
    }
}