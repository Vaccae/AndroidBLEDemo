package vac.test.bluetoothbledemo.repository

import kotlinx.coroutines.flow.merge
import vac.test.bluetoothbledemo.bytearraytoint

object BLEByteArrayUtil {

    //计算发送的数据库生成数组
    fun calcSendbyteArray(byteArray: ByteArray): Array<ByteArray?> {
        //根据当前的传输MTU值计算要分的包数
        //分包格式前前两个byte是总包数，当前包数,
        //为了节省字节，前4个字节为总包数2个，当前包数2个，
        //采用short的取值范围65536，分包如果超过这个总包数，就不做传输了
        val everybytelen = BlueToothBLEUtil.mtuSize - 4
        val totalpkgs =
            byteArray.size / everybytelen + if (byteArray.size % everybytelen > 0) 1 else 0

        val listbyte = byteArray.toList().chunked(everybytelen)
        val arybytes = arrayOfNulls<ByteArray>(totalpkgs)
        //定义总包数
        val totalbytepkgs = inttobytes2bit(totalpkgs)
        for(i in 0 until totalpkgs) {
            //转换当前包数
            val curbytepkgs = inttobytes2bit(i)
            val curbytes = totalbytepkgs.plus(curbytepkgs).plus(listbyte[i])
            arybytes[i] = curbytes
        }
        return arybytes
    }


    //region 处理接收的数组
    //获取当前ByteArray的总包数
    fun getTotalpkgs(bytes: ByteArray):Int{
        val totalbytes = bytes.slice(0..1)
        return bytearraytoint(totalbytes.toByteArray())
    }

    //获取当前ByteArray的当前包数
    fun getCurpkgs(bytes: ByteArray):Int{
        val curbytes = bytes.slice(2..3)
        return bytearraytoint(curbytes.toByteArray())
    }

    //获取当前ByteArray的实际数据包
    fun getByteArray(bytes: ByteArray):ByteArray{
        val curdata = bytes.slice(4 until bytes.size)
        return curdata.toByteArray()
    }
    //endregion

    //Int类型转ByteArray，范围是65536，只用两个字节
    private fun inttobytes2bit(num: Int): ByteArray {
        val byteArray = ByteArray(2)
        val lowH = ((num shr 8) and 0xff).toByte()
        val lowL = (num and 0xff).toByte()
        byteArray[0] = lowH
        byteArray[1] = lowL
        return byteArray
    }

    //ByteArray类型转Int，范围是65536，只用两个字节
    private fun bytestoint2bit(bytes: ByteArray): Int {
        val lowH = (bytes[0].toInt() shl 8)
        val lowl = bytes[1].toInt()

        val resint = if (lowH + lowl < 0) {
            65536 + lowH + lowl
        } else {
            lowH + lowl
        }
        return resint
    }

}