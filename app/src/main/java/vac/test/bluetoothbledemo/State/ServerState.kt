package vac.test.bluetoothbledemo.State

sealed class ServerState{

    data class Info(val msg: String?) : ServerState()

    data class Error(val msg: String?) : ServerState()
}
