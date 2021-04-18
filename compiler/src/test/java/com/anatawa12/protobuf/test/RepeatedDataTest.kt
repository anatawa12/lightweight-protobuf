package com.anatawa12.protobuf.test

import com.anatawa12.protobuf.Bytes
import com.anatawa12.protobuf.ProtobufReader
import com.anatawa12.protobuf.test.google.First
import com.anatawa12.protobuf.test.lightweight.RepeatedData
import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class RepeatedDataTest {
    @Test
    fun fromGoogleToLight() {
        val google = googleValue()
        val baos = ByteArrayOutputStream()
        google.writeTo(baos)
        val light = RepeatedData.parseFrom(ProtobufReader(ByteArrayInputStream(baos.toByteArray())))

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

    /*
    @Test
    fun fromLightToGoogle() {
        val light = lightValue()
        val baos = ByteArrayOutputStream()
        light.writeTo(baos)
        val google = First.RepeatedData.parseFrom(ByteArrayInputStream(baos.toByteArray()))

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
    // */

    @Test
    fun emptiesAreEqual() {
        assertTrue(RepeatedData.newBuilder().build()
                == RepeatedData.newBuilder().build()) { "two empties are not equals" }
        assertTrue(RepeatedData.defaultValue == RepeatedData.newBuilder().build()) { "two empty and defaultValue are not equals" }
    }

    fun googleValue() = First.RepeatedData.newBuilder()
        .addDoubleValue(1.0)
        .addFloatValue(2.0F)
        .addInt64Value(3)
        .addUint64Value(4)
        .addFixed64Value(5)
        .addSfixed64Value(6)
        .addSint64Value(7)
        .addInt32Value(8)
        .addFixed32Value(9)
        .addSfixed32Value(10)
        .addUint32Value(11)
        .addSint32Value(12)
        .addBoolValue(true)
        .addStringValue("hello! this is test string!")
        .addBytesValue(ByteString.copyFrom("bytes data".toByteArray()))
        .build()

    fun lightValue() = RepeatedData.newBuilder()
        .addDoubleValue(1.0)
        .addFloatValue(2.0F)
        .addInt64Value(3)
        .addUint64Value(4)
        .addFixed64Value(5)
        .addSfixed64Value(6)
        .addSint64Value(7)
        .addInt32Value(8)
        .addFixed32Value(9)
        .addSfixed32Value(10)
        .addUint32Value(11)
        .addSint32Value(12)
        .addBoolValue(true)
        .addStringValue("hello! this is test string!")
        .addBytesValue(Bytes.wrapUnsafe("bytes data".toByteArray()))
        .build()
}
