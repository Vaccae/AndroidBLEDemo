package vac.test.bluetoothbledemo.adapter

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseDifferAdapter
import kotlinx.coroutines.launch
import vac.test.bluetoothbledemo.databinding.RclCharacteristicsBinding
import vac.test.bluetoothbledemo.databinding.RclGattserviceBinding
import vac.test.bluetoothbledemo.intent.ConnectIntent
import vac.test.bluetoothbledemo.repository.BlueToothBLEUtil
import vac.test.bluetoothbledemo.vm.ConnectViewModel

class GattServiceDiffCallback : DiffUtil.ItemCallback<BluetoothGattService>() {
    override fun areItemsTheSame(
        oldItem: BluetoothGattService,
        newItem: BluetoothGattService
    ): Boolean {
        // 判断是否是同一个 item（通常使用id字段判断）
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(
        oldItem: BluetoothGattService,
        newItem: BluetoothGattService
    ): Boolean {
        // 如果是同一个item，则判断item内的数据内容是否有变化
        return oldItem.characteristics == newItem.characteristics
    }

}

class GattServiceAdapter(viewModel: ConnectViewModel) :
    BaseDifferAdapter<BluetoothGattService, GattServiceAdapter.VH>(GattServiceDiffCallback()) {
    // 自定义ViewHolder类
    class VH(
        parent: ViewGroup,
        val binding: RclGattserviceBinding = RclGattserviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : RecyclerView.ViewHolder(binding.root)

    var connectViewModel = viewModel

    override fun onBindViewHolder(holder: VH, position: Int, item: BluetoothGattService?) {
        item?.let {
            //设置服务的UUID
            holder.binding.tvuuid.text = it.uuid.toString()
            //设置服务的类型
            var typestr = ""
            when (it.type) {
                BluetoothGattService.SERVICE_TYPE_PRIMARY -> typestr = "PRIMARY SERVICE"
                BluetoothGattService.SERVICE_TYPE_SECONDARY -> typestr = "SECONDARY SERVICE"
            }
            holder.binding.tvserviceType.text = typestr

            holder.binding.characteristicLayout.removeAllViews()
            //遍历特征
            it.characteristics?.let { characteristics ->
                for (charitem in characteristics) {
                    addCharacteristicLayout(
                        holder.itemView.context,
                        holder.binding.characteristicLayout,
                        charitem
                    )
                }
            }
        }
    }

    private fun addCharacteristicLayout(
        context: Context,
        characteristicLayout: LinearLayout,
        characteristic: BluetoothGattCharacteristic
    ) {
        val binding: RclCharacteristicsBinding = RclCharacteristicsBinding.inflate(
            LayoutInflater.from(context), characteristicLayout, false
        )
        //特征的uuiD
        binding.tvcharacteristicUUID.text = characteristic.uuid.toString()
        //获取特征属性
        val propertiesStr = getProperties(characteristic.properties)
        binding.tvcharacteristicProperties.text = propertiesStr

        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0){
            BlueToothBLEUtil.setCharacteristicNotify(characteristic , true)
        }

        //发送数据
        binding.btnwrite.setOnClickListener {
            connectViewModel.viewModelScope.launch {
                val sendmsg = binding.edtinput.text.toString()

                connectViewModel.connectIntent.send(
                    ConnectIntent.WriteCharacteristic(sendmsg, characteristic)
                )
            }
        }

        //读取数据
        binding.btnread.setOnClickListener {
            connectViewModel.viewModelScope.launch {
                connectViewModel.connectIntent.send(
                    ConnectIntent.ReadCharacteristic(characteristic)
                )
            }
        }

        //将当前特征具体的布局添加到特征容器布局中
        characteristicLayout.addView(binding.root)
    }

    /**
     * 获取具体属性
     */
    private fun getProperties(properties: Int): String {
        val buffer = StringBuffer()
        for (i in 1..8) {
            when (i) {
                1 -> if (properties and BluetoothGattCharacteristic.PROPERTY_BROADCAST != 0)
                    buffer.append("BROADCAST,")

                2 -> if (properties and BluetoothGattCharacteristic.PROPERTY_READ != 0)
                    buffer.append("READ,")

                3 -> if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0)
                    buffer.append("WRITE NO RESPONSE,")

                4 -> if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0)
                    buffer.append("WRITE,")

                5 -> if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
                    buffer.append("NOTIFY,")

                6 -> if (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0)
                    buffer.append("INDICATE,")

                7 -> if (properties and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE != 0)
                    buffer.append("SIGNED WRITE,")

                8 -> if (properties and BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS != 0)
                    buffer.append("EXTENDED PROPS,")
            }
        }
        val str = buffer.toString()
        return if (str.isNotEmpty())
            str.substring(0, str.length - 1)
        else
            ""
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): VH {
        // 返回一个 ViewHolder
        return VH(parent)
    }

}