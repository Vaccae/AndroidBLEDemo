package vac.test.bluetoothbledemo.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.EncodeUtil
import vac.test.bluetoothbledemo.bean.CPhone
import vac.test.bluetoothbledemo.databinding.FragmentServerBinding
import vac.test.bluetoothbledemo.State.PhoneState
import vac.test.bluetoothbledemo.intent.ServerIntent
import vac.test.bluetoothbledemo.State.ServerState
import vac.test.bluetoothbledemo.bytesToHexString
import vac.test.bluetoothbledemo.hexStringToBytes
import vac.test.bluetoothbledemo.repository.BlueToothBLEUtil
import vac.test.bluetoothbledemo.vm.ServerViewModel

class ServerFragment : BaseFragment<FragmentServerBinding>() {

    companion object {
        fun newInstance() = ServerFragment()
    }

    private lateinit var serverViewModel: ServerViewModel

    private var mPhone = CPhone()

    //region ViewBinding定义，防止内存泄露
    override val bindingInflater: (LayoutInflater, ViewGroup?, Bundle?) -> FragmentServerBinding
        get() = { layoutInflater, viewGroup, _ ->
            FragmentServerBinding.inflate(layoutInflater, viewGroup, false)
        }
    //endregion

    /**
     * 蓝牙广播回调类
     */
    private inner class advertiseCallback : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            //发送数据
            lifecycleScope.launch {
                //如果持续广播 则不提醒关闭
                if (BlueToothBLEUtil.Time != 0) {
                    lifecycleScope.async {
                        delay(BlueToothBLEUtil.Time.toLong())
                        serverViewModel.serverIntent.send(
                            ServerIntent.Info("BLE广播结束")
                        )
                    }
                }


                val advertiseInfo = StringBuffer("启动BLE广播成功")
                //连接性
                if (settingsInEffect.isConnectable) {
                    advertiseInfo.append(", 可连接")
                } else {
                    advertiseInfo.append(", 不可连接")
                }
                //广播时长
                if (settingsInEffect.timeout == 0) {
                    advertiseInfo.append(", 持续广播")
                } else {
                    advertiseInfo.append(", 广播时长 ${settingsInEffect.timeout} ms")
                }
                serverViewModel.serverIntent.send(
                    ServerIntent.Info(advertiseInfo.toString())
                )

            }
        }

        //具体失败返回码可以到官网查看
        override fun onStartFailure(errorCode: Int) {
            //发送数据
            lifecycleScope.launch {
                var errstr = ""
                if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                    errstr = "启动Ble广播失败 数据报文超出31字节"
                } else {
                    errstr = "启动Ble广播失败 errorCode = $errorCode"
                }

                serverViewModel.serverIntent.send(
                    ServerIntent.Error(errstr)
                )
            }
        }
    }

    /**
     * GattServer回调
     */
    private inner class bluetoothGattServerCallback : BluetoothGattServerCallback() {

        //设备连接/断开连接回调
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            Log.d("pkg", "Server status = $status  newState = $newState")
            lifecycleScope.launch {
                var msg = ""
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //连接成功
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        msg = "${device.address} 连接成功"
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        msg = "${device.address} 断开连接"
                    }
                    serverViewModel.serverIntent.send(
                        ServerIntent.Info(msg)
                    )
                } else {
                    msg = "onConnectionStateChange status = $status newState = $newState"
                    serverViewModel.serverIntent.send(
                        ServerIntent.Error(msg)
                    )
                }
            }
        }

        //添加本地服务回调
        override fun onServiceAdded(status: Int, service: BluetoothGattService) {
            super.onServiceAdded(status, service)
            lifecycleScope.launch {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    serverViewModel.serverIntent.send(
                        ServerIntent.Info("添加Gatt服务成功 UUUID = ${service.uuid}")
                    )
                } else {
                    serverViewModel.serverIntent.send(
                        ServerIntent.Error("添加Gatt服务失败")
                    )
                }
            }
        }

        //特征值读取回调
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Log.i("pkg","${requestId}")
            // 响应客户端
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mBluetoothGattServer.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, "001".toByteArray()
            )
            lifecycleScope.launch {
                serverViewModel.serverIntent.send(
                    ServerIntent.Info(
                        "${device.address} 请求读取特征值:  UUID = ${characteristic.uuid} " +
                                "读取值 = ${EncodeUtil.bytesToHexString(characteristic.value)}"
                    )
                )
            }
        }

        //特征值写入回调
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice, requestId: Int,
            characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean,
            responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            Log.i("pkg","${requestId}  ${value}")
            //刷新该特征值
            characteristic.value = value
            // 响应客户端
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
//            mBluetoothGattServer.sendResponse(
//                device, requestId, BluetoothGatt.GATT_SUCCESS,
//                offset, value
//            )

            var readstr = String(value)

            val resbytearray = "2${readstr}".toByteArray()
            BlueToothBLEUtil.sendResponse(
                device,requestId,offset,resbytearray
            )


            lifecycleScope.launch {
                serverViewModel.serverIntent.send(

                            ServerIntent.Info(
                            "${device.address} 请求写入特征值:  UUID = ${characteristic.uuid} " +
                                    "写入值 = ${ readstr}"
                            )
                )

                lifecycleScope.async {
                    //模拟数据处理，延迟100ms
                    delay(100)

                    readstr = "1$readstr"
                    val readbytearray = readstr.toByteArray()
                    characteristic.value = readbytearray

                    //回复客户端,让客户端读取该特征新赋予的值，获取由服务端发送的数据
//                    mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false)
                    BlueToothBLEUtil.notifyCharacteristicChanged(
                        device , characteristic, readbytearray
                    )
                }


            }
        }

        //描述读取回调
        override fun onDescriptorReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int,
            descriptor: BluetoothGattDescriptor
        ) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            // 响应客户端
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mBluetoothGattServer.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, descriptor.value
            )
            lifecycleScope.launch {
                ServerIntent.Info(
                    "${device.address} 请求读取描述值:  UUID = ${descriptor.uuid} " +
                            "读取值 = ${EncodeUtil.bytesToHexString(descriptor.value)}"
                )
            }
        }

        //描述写入回调
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice, requestId: Int, descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean, responseNeeded: Boolean,
            offset: Int, value: ByteArray
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            //刷新描述值
            descriptor.value = value
            // 响应客户端
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mBluetoothGattServer.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, value
            )
            lifecycleScope.launch {
                ServerIntent.Info(
                    "${device.address} 请求写入描述值:  UUID = ${descriptor.uuid} " +
                            "写入值 = ${EncodeUtil.bytesToHexString(value)}"
                )
            }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            lifecycleScope.launch {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    serverViewModel.serverIntent.send(
                        ServerIntent.Info("${device?.address} 通知发送成功")
                    )
                } else {
                    serverViewModel.serverIntent.send(
                        ServerIntent.Error("${device?.address} 通知发送失败 status = $status")
                    )
                }
            }


        }
    }


    //蓝牙广播回调类
    private lateinit var mAdvertiseCallback: advertiseCallback

    //GattServer回调
    private lateinit var mBluetoothGattServerCallback: BluetoothGattServerCallback

    //GattServer
    private lateinit var mBluetoothGattServer: BluetoothGattServer

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        serverViewModel = ViewModelProvider(requireActivity()).get(ServerViewModel::class.java)

        observeViewModel()

        initBluetooth()

        binding.btnstartAdvertising.setOnClickListener {
            lifecycleScope.launch {
                try {
                    if (!BlueToothBLEUtil.startAdvertising(
                            "${mPhone.manufacturer} ${mPhone.modelname}",
                            mAdvertiseCallback
                        )
                    ) {
                        serverViewModel.serverIntent.send(ServerIntent.Error("该手机芯片不支持BLE广播"))
                    }
                } catch (e: Exception) {
                    serverViewModel.serverIntent.send(ServerIntent.Error(e.message))
                }
            }
        }

        binding.btnstopAdvertising.setOnClickListener {
            lifecycleScope.launch {
                try {
                    if (BlueToothBLEUtil.stopAdvertising(mAdvertiseCallback)) {
                        serverViewModel.serverIntent.send(ServerIntent.Info("停止Ble广播"))
                    }
                } catch (e: Exception) {
                    serverViewModel.serverIntent.send(ServerIntent.Error(e.message))
                }
            }
        }

        binding.btnaddGattServer.setOnClickListener {
            lifecycleScope.launch {
                try {
                    BlueToothBLEUtil.addGattServer(mBluetoothGattServerCallback)
                } catch (e: Exception) {
                    serverViewModel.serverIntent.send(ServerIntent.Error(e.message))
                }
            }
        }
    }


    /**
     * 初始化蓝牙
     */
    private fun initBluetooth() {
        //初始化蓝牙回调包
        mAdvertiseCallback = advertiseCallback()
        //初始化GattServer回调
        mBluetoothGattServerCallback = bluetoothGattServerCallback()
    }


    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    serverViewModel.serverState.collect {
                        when (it) {
                            is ServerState.Info -> {
                                binding.tvMsgShow.append("$it\r\n")
                            }

                            is ServerState.Error -> {
                                //错误用红色字体标识
                                val spannableString = SpannableString(it.msg)
                                spannableString.setSpan(
                                    ForegroundColorSpan(Color.RED),
                                    0,
                                    spannableString.length,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                                )
                                binding.tvMsgShow.append("$spannableString\r\n")
                            }
                        }
                    }
                }
                launch {
                    serverViewModel.phone.collect {
                        when (it) {
                            is PhoneState.Idle -> {
                                mPhone = CPhone()
                            }

                            is PhoneState.Loading -> {

                            }

                            is PhoneState.Phone -> {
                                mPhone = it.device
                            }

                            is PhoneState.Error -> {
                                Toast.makeText(requireContext(), it.error, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }
                    }
                }
            }
        }
    }
}