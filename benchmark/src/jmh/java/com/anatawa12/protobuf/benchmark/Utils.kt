package com.anatawa12.protobuf.benchmark

import com.anatawa12.protobuf.Bytes
import com.anatawa12.protobuf.benchmark.google.First
import okio.ByteString as OKByteString
import com.google.protobuf.ByteString as GGByteString
import com.anatawa12.protobuf.benchmark.google.First.SimpleData as GGSimpleData
import com.anatawa12.protobuf.benchmark.wire.SimpleData as WRSimpleData
import com.anatawa12.protobuf.benchmark.lightweight.SimpleData as LWSimpleData
import kotlin.random.Random

fun Random.nextString() = nextBytes(nextInt(100)).toHex()
fun Random.nextBytes() = nextBytes(nextInt(100))
fun Random.getNextDouble(): Double = nextDouble(Double.MIN_VALUE, Double.MAX_VALUE)
fun Random.getNextFloat(): Float {
    while (true) {
        val value = java.lang.Float.intBitsToFloat(nextInt())
        if (value.isFinite()) return value
    }
}

private val hexArray = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex(): String {
    val hexChars = CharArray(this.size * 2 + this.size / 8)
    var i = 0
    for (j in this.indices) {
        val v = this[j].toInt() and 0xFF

        hexChars[i++] = hexArray[v ushr 4]
        hexChars[i++] = hexArray[v and 0x0F]
        if (j % 8 == 7) hexChars[i++] = ' '
    }
    return String(hexChars)
}

fun createTriple(
    doubleValue: Double,
    floatValue: Float,
    int64Value: Long,
    uint64Value: Long,
    fixed64Value: Long,
    sfixed64Value: Long,
    sint64Value: Long,
    int32Value: Int,
    fixed32Value: Int,
    sfixed32Value: Int,
    uint32Value: Int,
    sint32Value: Int,
    boolValue: Boolean,
    stringValue: String,
    bytesValue: ByteArray,
): Triple<com.anatawa12.protobuf.benchmark.lightweight.SimpleData, First.SimpleData, com.anatawa12.protobuf.benchmark.wire.SimpleData> {
    val lw = LWSimpleData.newBuilder()
        .setDoubleValue(doubleValue)
        .setFloatValue(floatValue)
        .setInt64Value(int64Value)
        .setUint64Value(uint64Value)
        .setFixed64Value(fixed64Value)
        .setSfixed64Value(sfixed64Value)
        .setSint64Value(sint64Value)
        .setInt32Value(int32Value)
        .setFixed32Value(fixed32Value)
        .setSfixed32Value(sfixed32Value)
        .setUint32Value(uint32Value)
        .setSint32Value(sint32Value)
        .setBoolValue(boolValue)
        .setStringValue(stringValue)
        .setBytesValue(Bytes.wrapUnsafe(bytesValue))
        .build()
    val gg = GGSimpleData.newBuilder()
        .setDoubleValue(doubleValue)
        .setFloatValue(floatValue)
        .setInt64Value(int64Value)
        .setUint64Value(uint64Value)
        .setFixed64Value(fixed64Value)
        .setSfixed64Value(sfixed64Value)
        .setSint64Value(sint64Value)
        .setInt32Value(int32Value)
        .setFixed32Value(fixed32Value)
        .setSfixed32Value(sfixed32Value)
        .setUint32Value(uint32Value)
        .setSint32Value(sint32Value)
        .setBoolValue(boolValue)
        .setStringValue(stringValue)
        .setBytesValue(GGByteString.copyFrom(bytesValue))
        .build()
    val wr = WRSimpleData.Builder()
        .doubleValue(doubleValue)
        .floatValue(floatValue)
        .int64Value(int64Value)
        .uint64Value(uint64Value)
        .fixed64Value(fixed64Value)
        .sfixed64Value(sfixed64Value)
        .sint64Value(sint64Value)
        .int32Value(int32Value)
        .fixed32Value(fixed32Value)
        .sfixed32Value(sfixed32Value)
        .uint32Value(uint32Value)
        .sint32Value(sint32Value)
        .boolValue(boolValue)
        .stringValue(stringValue)
        .bytesValue(OKByteString.run { bytesValue.toByteString() })
        .build()
    return Triple(lw, gg, wr)
}
