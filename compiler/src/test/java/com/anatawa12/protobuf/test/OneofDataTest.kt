package com.anatawa12.protobuf.test

import com.anatawa12.protobuf.Bytes
import com.anatawa12.protobuf.ProtobufReader
import com.anatawa12.protobuf.test.google.First
import com.anatawa12.protobuf.test.lightweight.OneofData
import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class OneofDataTest {
    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15])
    fun fromGoogleToLight(id: Int) {
        val google = googleValue(id)
        val baos = ByteArrayOutputStream()
        google.writeTo(baos)
        val light = OneofData.parseFrom(ProtobufReader(ByteArrayInputStream(baos.toByteArray())))

        assertEquals(lightValue(id), light) {
            """
                expected: ${lightValue(id)}
                google:
                $google
                light: $light
                encoded: ${bytesToHex(baos.toByteArray())}
            """.trimIndent()
        }
    }

    /*
    @ParameterizedTest
    @ValueSource(ints = [1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15])
    fun fromLightToGoogle(id: Int) {
        val light = lightValue(id)
        val baos = ByteArrayOutputStream()
        light.writeTo(baos)
        val google = First.OneofData.parseFrom(ByteArrayInputStream(baos.toByteArray()))

        assertEquals(googleValue(id), google) {
            """
                expected:
                ${googleValue(id)}
                google:
                $google
                light: $light
                encoded: ${bytesToHex(baos.toByteArray())}
            """.trimIndent()
        }
    }
    // */

    @Test
    fun emptiesAreEqual() {
        assertTrue(OneofData.newBuilder().build()
                == OneofData.newBuilder().build()) { "two empties are not equals" }
        assertTrue(OneofData.defaultValue == OneofData.newBuilder()
            .build()) { "two empty and defaultValue are not equals" }
    }

    @Test
    fun nanEqualsDouble() {
        assertTrue(OneofData.newBuilder().setDoubleValue(Double.NaN).build()
                == OneofData.newBuilder().setDoubleValue(Double.NaN).build()) { "two NaNs are not equals" }
    }

    @Test
    fun nanEqualsFloat() {
        assertTrue(OneofData.newBuilder().setFloatValue(Float.NaN).build()
                == OneofData.newBuilder().setFloatValue(Float.NaN).build()) { "two NaNs are not equals" }
    }

    fun googleValue(id: Int) = when (id) {
        1 -> First.OneofData.newBuilder().setDoubleValue(1.0).build()
        2 -> First.OneofData.newBuilder().setFloatValue(2.0F).build()
        3 -> First.OneofData.newBuilder().setInt64Value(3).build()
        4 -> First.OneofData.newBuilder().setUint64Value(4).build()
        5 -> First.OneofData.newBuilder().setFixed64Value(5).build()
        6 -> First.OneofData.newBuilder().setSfixed64Value(6).build()
        7 -> First.OneofData.newBuilder().setSint64Value(7).build()
        8 -> First.OneofData.newBuilder().setInt32Value(8).build()
        9 -> First.OneofData.newBuilder().setFixed32Value(9).build()
        10 -> First.OneofData.newBuilder().setSfixed32Value(10).build()
        11 -> First.OneofData.newBuilder().setUint32Value(11).build()
        12 -> First.OneofData.newBuilder().setSint32Value(12).build()
        13 -> First.OneofData.newBuilder().setBoolValue(true).build()
        14 -> First.OneofData.newBuilder().setStringValue("hello! this is test string!").build()
        15 -> First.OneofData.newBuilder().setBytesValue(ByteString.copyFrom("bytes data".toByteArray())).build()
        else -> error("invalid test")
    }

    fun lightValue(id: Int) = when (id) {
        1 -> OneofData.newBuilder().setDoubleValue(1.0).build()
        2 -> OneofData.newBuilder().setFloatValue(2.0F).build()
        3 -> OneofData.newBuilder().setInt64Value(3).build()
        4 -> OneofData.newBuilder().setUint64Value(4).build()
        5 -> OneofData.newBuilder().setFixed64Value(5).build()
        6 -> OneofData.newBuilder().setSfixed64Value(6).build()
        7 -> OneofData.newBuilder().setSint64Value(7).build()
        8 -> OneofData.newBuilder().setInt32Value(8).build()
        9 -> OneofData.newBuilder().setFixed32Value(9).build()
        10 -> OneofData.newBuilder().setSfixed32Value(10).build()
        11 -> OneofData.newBuilder().setUint32Value(11).build()
        12 -> OneofData.newBuilder().setSint32Value(12).build()
        13 -> OneofData.newBuilder().setBoolValue(true).build()
        14 -> OneofData.newBuilder().setStringValue("hello! this is test string!").build()
        15 -> OneofData.newBuilder().setBytesValue(Bytes.wrapUnsafe("bytes data".toByteArray())).build()
        else -> error("invalid test")
    }
}
