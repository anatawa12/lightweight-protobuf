package com.anatawa12.protobuf.test

import com.anatawa12.protobuf.Bytes
import com.anatawa12.protobuf.WireReader
import com.anatawa12.protobuf.test.google.First
import com.anatawa12.protobuf.test.lightweight.SimpleData
import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SimpleDataTest {
    @Test
    fun fromGoogleToLight() {
        val google = googleValue()
        val baos = ByteArrayOutputStream()
        google.writeTo(baos)
        val light = SimpleData.parseFrom(WireReader(ByteArrayInputStream(baos.toByteArray())))

        assertEquals(lightValue(), light) {
            """
                expected: ${lightValue()}
                google:
                $google
                light: $light
                encoded: ${bytesToHex(baos.toByteArray())}
            """.trimIndent()
        }
    }

    @Test
    fun fromLightToGoogle() {
        val light = lightValue()
        val baos = ByteArrayOutputStream()
        light.writeTo(baos)
        val google = First.SimpleData.parseFrom(ByteArrayInputStream(baos.toByteArray()))

        assertEquals(googleValue(), google) {
            """
                expected:
                ${googleValue()}
                google:
                $google
                light: $light
                encoded: ${bytesToHex(baos.toByteArray())}
            """.trimIndent()
        }
    }

    @Test
    fun emptiesAreEqual() {
        assertTrue(SimpleData.newBuilder().build()
                == SimpleData.newBuilder().build()) { "two empties are not equals" }
        assertTrue(SimpleData.defaultValue == SimpleData.newBuilder()
            .build()) { "two empty and defaultValue are not equals" }
    }

    @Test
    fun nanEqualsDouble() {
        assertTrue(SimpleData.newBuilder().setDoubleValue(Double.NaN).build()
                == SimpleData.newBuilder().setDoubleValue(Double.NaN).build()) { "two NaNs are not equals" }
    }

    @Test
    fun nanEqualsFloat() {
        assertTrue(SimpleData.newBuilder().setFloatValue(Float.NaN).build()
                == SimpleData.newBuilder().setFloatValue(Float.NaN).build()) { "two NaNs are not equals" }
    }

    fun googleValue() = First.SimpleData.newBuilder()
        .setDoubleValue(1.0)
        .setFloatValue(2.0F)
        .setInt64Value(1003)
        .setUint64Value(4)
        .setFixed64Value(5)
        .setSfixed64Value(6)
        .setSint64Value(7)
        .setInt32Value(8)
        .setFixed32Value(9)
        .setSfixed32Value(10)
        .setUint32Value(11)
        .setSint32Value(-12)
        .setBoolValue(true)
        .setStringValue("hello! this is test string!")
        .setBytesValue(ByteString.copyFrom("bytes data".toByteArray()))
        .build()

    fun lightValue() = SimpleData.newBuilder()
        .setDoubleValue(1.0)
        .setFloatValue(2.0F)
        .setInt64Value(1003)
        .setUint64Value(4)
        .setFixed64Value(5)
        .setSfixed64Value(6)
        .setSint64Value(7)
        .setInt32Value(8)
        .setFixed32Value(9)
        .setSfixed32Value(10)
        .setUint32Value(11)
        .setSint32Value(-12)
        .setBoolValue(true)
        .setStringValue("hello! this is test string!")
        .setBytesValue(Bytes.wrapUnsafe("bytes data".toByteArray()))
        .build()
}
