package vac.test.bluetoothbledemo

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

    val unicode = str.toCharArray().map { encode(it) }
        .joinToString(separator = "", truncated = "")

    print("$str  转换后：$unicode")

    val decodestr = decode(unicode)
    println("转换回后：$decodestr")

    val bytearr = str.toByteArray(charset = Charsets.UTF_8)
    val bytestr = bytesToHexString(bytearr)
    println(bytestr)

    val bytechange = hexStringToBytes(bytestr!!)
    val finalstr = String(bytechange!!, Charsets.UTF_8)
    println(finalstr)
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

