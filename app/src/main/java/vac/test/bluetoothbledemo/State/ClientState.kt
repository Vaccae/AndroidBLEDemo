package vac.test.bluetoothbledemo.State

import vac.test.bluetoothbledemo.bean.CBleDevice

sealed class ClientState {
    object ScanMode : ClientState()
    object ConnectMode : ClientState()
    object DisConnect : ClientState()
    data class Connect(val dev: CBleDevice) : ClientState()
    data class Error(val error: String?) : ClientState()
}
