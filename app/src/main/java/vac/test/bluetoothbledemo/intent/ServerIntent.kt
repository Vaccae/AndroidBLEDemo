package vac.test.bluetoothbledemo.intent

sealed class ServerIntent{

    data class Info(val msg: String?) : ServerIntent()

    data class Error(val msg: String?) : ServerIntent()

    data class RecvByteArray(val bytes: ByteArray?) :ServerIntent()
}
