package vac.test.bluetoothbledemo.adapter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseDifferAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import vac.test.bluetoothbledemo.bean.CBleDevice
import vac.test.bluetoothbledemo.databinding.RclBledeviceBinding
import vac.test.bluetoothbledemo.repository.BlueToothBLEUtil

class BleDeviceDiffCallback : DiffUtil.ItemCallback<CBleDevice>() {
    override fun areItemsTheSame(oldItem: CBleDevice, newItem: CBleDevice): Boolean {
        // 判断是否是同一个 item（通常使用id字段判断）
        return oldItem.device == newItem.device
    }

    override fun areContentsTheSame(oldItem: CBleDevice, newItem: CBleDevice): Boolean {
        // 如果是同一个item，则判断item内的数据内容是否有变化
        return oldItem.rssi == newItem.rssi
    }

}

class BleDeviceAdapter :
    BaseDifferAdapter<CBleDevice, BleDeviceAdapter.VH>(BleDeviceDiffCallback()) {
    // 自定义ViewHolder类
    class VH(
        parent: ViewGroup,
        val binding: RclBledeviceBinding = RclBledeviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: VH, position: Int, item: CBleDevice?) {
        item?.let {
            if (!BlueToothBLEUtil.checkBlueToothPermission(Manifest.permission.BLUETOOTH_CONNECT)) return@let
            holder.binding.rclnametext.text = it.device?.name ?: "N/A"
            holder.binding.rclrssiText.text = "${it.rssi}"
            when (it.device?.bondState) {
                10 ->
                    holder.binding.rclbondStateText.text = "Not BONDED"

                12 ->
                    holder.binding.rclbondStateText.text = "BONDED"

                else ->
                    holder.binding.rclbondStateText.text = "Not BONDED"
            }
            //设置mac地址
            holder.binding.rclmacAddressText.text = it.device?.address
            //判断是否可以连接
            if (!it.isConnectable) {
                holder.binding.rclconnecBtn.visibility = View.INVISIBLE
            } else {
                holder.binding.rclconnecBtn.visibility = View.VISIBLE
            }
            //判断厂商类型
            it.scanRecord?.let { rcd ->
                //判断是否是苹果的厂商id
                if (rcd?.getManufacturerSpecificData(0x4C) != null) {
                    holder.binding.rclFactoryText.text = "苹果"
                } else if (rcd?.getManufacturerSpecificData(0x06) != null) {
                    holder.binding.rclFactoryText.text = "微软"
                } else {
                    holder.binding.rclFactoryText.text = "蓝牙"
                }

            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): VH {
        // 返回一个 ViewHolder
        return VH(parent)
    }
}