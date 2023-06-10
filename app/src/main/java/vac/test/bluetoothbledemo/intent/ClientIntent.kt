package vac.test.bluetoothbledemo.intent

import vac.test.bluetoothbledemo.bean.CBleDevice

sealed class ClientIntent {
    //扫描界面
    object ScanMode: ClientIntent()
    //连接界面
    object ConnectMode:ClientIntent()
    //连接
    data class Connect(val dev: CBleDevice) : ClientIntent()

    //关闭连接
    object DisConnect : ClientIntent()

    //处理异常
    data class Error(val msg: String?) : ClientIntent()
}
