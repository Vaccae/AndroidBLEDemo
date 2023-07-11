package vac.test.bluetoothbledemo

import java.nio.ByteBuffer
import java.util.Arrays
import kotlin.experimental.and

fun Int.toHexString(): String = Integer.toHexString(this)

fun encode(char: Char) = "\\u${char.toInt().toHexString()}"
fun encode(byte: Byte) = byte.toInt().toHexString()

//unicode ->String
fun decode(encodeText: String): String {
    fun decode1(unicode: String) = unicode.toInt(16).toChar()
    val unicodes = encodeText.split("\\u")
        .map { if (it.isNotBlank()) decode1(it) else null }.filterNotNull()
    return String(unicodes.toCharArray())
}


fun main() {

    val str = "我明天给你做饭吃！"

//    val byte1 = byteArrayOf(1, 2, 3, 4, 5)
//    val byte2 = byteArrayOf(6, 7)
//    val byte3 = byteArrayOf(8, 9)
//
//    val array = arrayOf(byte1, byte2, byte3)
//
//    var byteArray = byteArrayOf()
//
//    for (cur in array) {
//        byteArray += cur
//    }
//
//    println(byteArray)
//

//
//    val unicode = str.toCharArray().map { encode(it) }
//        .joinToString(separator = "", truncated = "")
//
//    print("$str  转换后：$unicode")
//
//    val decodestr = decode(unicode)
//    println("转换回后：$decodestr")
//
    val int = 5
    val byte = inttobytearray(int)
    println(byte)


    val tempint = ByteBuffer.wrap(byte).short
    val tmpint = bytearraytoint(byte)
    println(tmpint)

    val tt = ByteBuffer.wrap(byte).short.toUShort()
    println(tt)


    val bytearr = str.toByteArray(charset = Charsets.UTF_8)
//    val bytestr = bytesToHexString(bytearr)
    println(bytearr)

    val lstbytearr = bytearr.toList().chunked(5)
    for (item in lstbytearr){
        val temp = item.toByteArray()
        println(temp)
    }

    for (i in lstbytearr.indices){
        val tmparr = arrayListOf(lstbytearr.size.toByte(), i.toByte())
        tmparr.addAll(lstbytearr[i])
        val arrres = tmparr.toByteArray()
        println(arrres)
    }

//    val bytechange = hexStringToBytes(bytestr!!)
//    val finalstr = String(bytechange!!, Charsets.UTF_8)
//    println(finalstr)

//    val lst = arrayListOf("1","2","3")
//    println("原始")
//    lst.forEach {
//        println(it)
//    }
//    val arr = lst.toTypedArray()
//    change(arr)
//    println("修改")
//    val newlst = arr.asList()
//    newlst.forEach {
//        println(it)
//    }
}


fun inttobytearray(num: Int): ByteArray {
    val byteArray = ByteArray(2)
    val lowH = ((num shr 8) and 0xff).toByte()
    val lowL = (num and 0xff).toByte()
    byteArray[0] = lowH
    byteArray[1] = lowL
    return byteArray
}

fun bytearraytoint(bytes: ByteArray): Int {
    val lowH = (bytes[0].toInt() shl 8)
    val lowl = bytes[1].toInt()

    var resint = if (lowH + lowl < 0) {
        65536 + lowH + lowl
    } else {
        lowH + lowl
    }

    return resint
}

fun change(arr: Array<String>) {
    for (i in arr.indices) {
        arr[i] += "kkk"
    }
}

fun bytesToHexString(src: ByteArray?): String? {
    if (src == null || src.isEmpty()) {
        return ""
    }
    val stringBuilder = StringBuilder("")
    if (src.isEmpty()) {
        return null
    }
    for (i in src.indices) {
        val v = src[i].toInt() and 0xFF
        val hex = Integer.toHexString(v)
        if (hex.length < 2) {
            stringBuilder.append(0)
        }
        stringBuilder.append(hex)
    }
    return stringBuilder.toString()
}

fun hexStringToBytes(str: String): ByteArray? {
    val abyte0 = ByteArray(str.length / 2)
    val s11 = str.toByteArray()
    for (i1 in 0 until s11.size / 2) {
        var byte1 = s11[i1 * 2 + 1]
        var byte0 = s11[i1 * 2]
        var s2: String
        abyte0[i1] = ((java.lang.Byte.decode(
            java.lang.StringBuilder("0x".also { s2 = it }.toString())
                .append(String(byteArrayOf(byte0))).toString()
        )
            .toByte().toInt() shl 4).toByte().also { byte0 = it }.toInt() xor
                java.lang.Byte.decode(
                    java.lang.StringBuilder(s2)
                        .append(String(byteArrayOf(byte1))).toString()
                ).toByte().also { byte1 = it }.toInt()).toByte()
    }
    return abyte0
}

