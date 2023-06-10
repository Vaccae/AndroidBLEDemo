package vac.test.bluetoothbledemo.ui

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.R
import vac.test.bluetoothbledemo.State.ClientState
import vac.test.bluetoothbledemo.State.ConnectState
import vac.test.bluetoothbledemo.adapter.GattServiceAdapter
import vac.test.bluetoothbledemo.databinding.FragmentConnectBinding
import vac.test.bluetoothbledemo.intent.ConnectIntent
import vac.test.bluetoothbledemo.repository.BlueToothBLEUtil
import vac.test.bluetoothbledemo.vm.ClientViewModel
import vac.test.bluetoothbledemo.vm.ConnectViewModel

class ConnectFragment : BaseFragment<FragmentConnectBinding>() {

    companion object {
        fun newInstance() = ConnectFragment()
    }

    private lateinit var connectViewModel: ConnectViewModel
    private lateinit var clientViewModel: ClientViewModel

    private lateinit var mAdapter: GattServiceAdapter

    //标记重置次数
    private var retryCount = 0

    override val bindingInflater: (LayoutInflater, ViewGroup?, Bundle?) -> FragmentConnectBinding
        get() = { layoutInflater, viewGroup, _ ->
            FragmentConnectBinding.inflate(layoutInflater, viewGroup, false)
        }

    /**
     * Gatt回调
     */
    val bleGattCallBack = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            lifecycleScope.launch {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        //连接状态
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            gatt?.let {
                                connectViewModel.connectIntent.send(
                                    ConnectIntent.Connect(it)
                                )
                            }
                        } else {
                            gatt?.let {
                                it.disconnect()

                                connectViewModel.connectIntent.send(
                                    ConnectIntent.DisConnect
                                )
                            }
                        }
                    }

                    else -> {
                        connectViewModel.connectIntent.send(
                            ConnectIntent.DisConnect
                        )
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            lifecycleScope.launch {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        gatt?.let {
                            connectViewModel.connectIntent.send(
                                ConnectIntent.Discovered(it.services)
                            )
                        }
                    }

                    else -> {
                        connectViewModel.connectIntent.send(
                            ConnectIntent.Error("发现服务失败！")
                        )
                    }
                }

            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            lifecycleScope.launch {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        val str = "返回消息：${String(value)}"
                        connectViewModel.connectIntent.send(
                            ConnectIntent.ReadCharacteristic(characteristic)
                        )
                    }
                    //无可读权限
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {

                    }
                    else -> {
                        connectViewModel.connectIntent.send(
                            ConnectIntent.Error("权限读取失败")
                        )
                    }
                }
            }

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)

            lifecycleScope.launch {
                val str = "返回消息：${String(value)}"
                connectViewModel.connectIntent.send(
                    ConnectIntent.CharacteristicNotify(str, characteristic)
                )
            }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectViewModel = ViewModelProvider(requireActivity()).get(ConnectViewModel::class.java)
        clientViewModel = ViewModelProvider(requireActivity()).get(ClientViewModel::class.java)
        // TODO: Use the ViewModel
        observeViewModel()

        mAdapter = GattServiceAdapter(connectViewModel)
        mAdapter.setEmptyViewLayout(requireContext(), R.layout.rcl_gattservice)
        mAdapter.submitList(null)

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = mAdapter

        binding.btnDo.setOnClickListener {
            lifecycleScope.launch {
                if (!connectViewModel.isConnect) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@launch
                    }
                    retryCount = 0
                    Log.i("pkg", "${clientViewModel.curBleDevice.device?.name}")

                    clientViewModel.curBleDevice.device?.let {
                        BlueToothBLEUtil.connect(
                            it.address,
                            bleGattCallBack
                        )
                    }
                } else {
                    BlueToothBLEUtil.disConnect()
                    connectViewModel.connectIntent.send(
                        ConnectIntent.DisConnect
                    )
                }
            }

        }

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    connectViewModel.connectState.collect {
                        when (it) {
                            is ConnectState.Idle -> {
                                binding.btnDo.text ="连接"
                                binding.tvMsgShow.text = "断开连接"
                            }
                            is ConnectState.Connect -> {
                                if (ActivityCompat.checkSelfPermission(
                                        requireContext(),
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    return@collect
                                }
                                it.gatt.discoverServices()
                                binding.btnDo.text ="断开连接"
                                binding.tvMsgShow.text = "${it.gatt.device.name}连接成功"
                            }

                            is ConnectState.Info ->{
                                binding.tvMsgShow.text = it.info
                            }

                            is ConnectState.Discovered -> {
                                mAdapter.submitList(it.gattservices)
                            }

                            is ConnectState.Error -> {
                                Toast.makeText(requireContext(), it.error, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
                launch {
                    clientViewModel.clientState.collect {
                        Log.i("pkg", "connectclientfragment ${it}")
                        when (it) {
                            is ClientState.ScanMode -> {
                                //replaceFragment(scanFragment)
                            }

                            is ClientState.ConnectMode -> {
                                //replaceFragment(connectFragment)
                            }

                            is ClientState.Connect -> {
                                binding.tvDeviceName.text =
                                    clientViewModel.curBleDevice.device?.name
                            }

                            else -> {

                            }
                        }
                    }
                }
            }
        }
    }

}