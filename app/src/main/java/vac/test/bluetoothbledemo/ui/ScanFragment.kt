package vac.test.bluetoothbledemo.ui

import android.Manifest
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.R
import vac.test.bluetoothbledemo.adapter.BleDeviceAdapter
import vac.test.bluetoothbledemo.databinding.FragmentScanBinding
import vac.test.bluetoothbledemo.intent.ScanIntent
import vac.test.bluetoothbledemo.State.BleDeviceState
import vac.test.bluetoothbledemo.intent.ClientIntent
import vac.test.bluetoothbledemo.repository.BlueToothBLEUtil
import vac.test.bluetoothbledemo.vm.ClientViewModel
import vac.test.bluetoothbledemo.vm.ConnectViewModel
import vac.test.bluetoothbledemo.vm.ScanViewModel

class ScanFragment : BaseFragment<FragmentScanBinding>() {

    companion object {
        fun newInstance() = ScanFragment()
    }

    private lateinit var mAdapter: BleDeviceAdapter
    private lateinit var scanViewModel: ScanViewModel
    private lateinit var clientViewModel: ClientViewModel
    private lateinit var connectViewModel: ConnectViewModel

    private var times = 0

    private val scanListener = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

            try {
                result?.let {
                    if(!BlueToothBLEUtil.checkBlueToothPermission(Manifest.permission.BLUETOOTH_CONNECT)) return@let
                    //发送数据
                    lifecycleScope.launch {
                        scanViewModel.actIntent.send(
                            ScanIntent.BleDevice(
                                it.device,
                                it.rssi, it.scanRecord!!.bytes, it.isConnectable, it.scanRecord
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    //region ViewBinding定义，防止内存泄露
    override val bindingInflater: (LayoutInflater, ViewGroup?, Bundle?) -> FragmentScanBinding
        get() = { layoutinfalter, viewGroup, _ ->
            FragmentScanBinding.inflate(layoutinfalter, viewGroup, false)
        }

    //endregion


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        scanViewModel = ViewModelProvider(this).get(ScanViewModel::class.java)
        clientViewModel = ViewModelProvider(requireActivity()).get(ClientViewModel::class.java)
        connectViewModel = ViewModelProvider(this).get(ConnectViewModel::class.java)
        // TODO: Use the ViewModel


        mAdapter = BleDeviceAdapter()
        mAdapter.setEmptyViewLayout(requireContext(), R.layout.rcl_bledevice)
        mAdapter.submitList(null)
        mAdapter.addOnItemChildClickListener(R.id.rclconnecBtn) { adapter, view, position ->
            lifecycleScope.launch {
                //连接时要先关闭扫描
                BlueToothBLEUtil.stopScanBlueToothDevice(scanListener)
                val dev = adapter.getItem(position)
                dev?.let {
                    clientViewModel.clientIntent.send(ClientIntent.Connect(it))
                }
            }
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = mAdapter

        observeViewModel()

        binding.btnbluetoothscan.setOnClickListener {
            lifecycleScope.launch {
                scanViewModel.actIntent.send(ScanIntent.InitBleDevices)
                BlueToothBLEUtil.scanBlueToothDevice(scanListener)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    scanViewModel.bleDeviceState.collect {
                        Log.i("pkg", "scan ${it}")
                        when (it) {
                            is BleDeviceState.BleDevices -> {
                                mAdapter.submitList(it.devices)
                            }

                            is BleDeviceState.Loading -> {
                                mAdapter.submitList(null)
                            }

                            is BleDeviceState.Error -> {
                                Toast.makeText(requireContext(), it.error, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            else -> {
                                Toast.makeText(requireContext(), "nothing", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }
}
