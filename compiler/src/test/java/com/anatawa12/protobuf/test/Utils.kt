package com.anatawa12.protobuf.test

private val hexArray = "0123456789ABCDEF".toCharArray()

fun bytesToHex(bytes: ByteArray): String {
    val hexChars = CharArray(bytes.size * 2 + bytes.size / 8)
    var i = 0
    for (j in bytes.indices) {
        val v = bytes[j].toInt() and 0xFF

        hexChars[i++] = hexArray[v ushr 4]
        hexChars[i++] = hexArray[v and 0x0F]
        if (j % 8 == 7) hexChars[i++] = ' '
    }
    return String(hexChars)
}
