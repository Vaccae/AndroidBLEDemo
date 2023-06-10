package vac.test.bluetoothbledemo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import vac.test.bluetoothbledemo.repository.BlueToothBLEUtil

class BaseApp : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: BaseApp
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        mContext = this

        //初始化BlueToothBLEUtil
        BlueToothBLEUtil.init(this)
    }
}