package com.anatawa12.protobuf.test

import com.anatawa12.protobuf.Bytes
import com.anatawa12.protobuf.ProtobufReader
import com.anatawa12.protobuf.test.google.First
import com.anatawa12.protobuf.test.lightweight.MapData
import com.google.protobuf.ByteString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.RuntimeException

class MapDataTest {
    @Test
    fun fromGoogleToLight() {
        val google = googleValue()
        val baos = ByteArrayOutputStream()
        google.writeTo(baos)
        val light = MapData.parseFrom(ProtobufReader(ByteArrayInputStream(baos.toByteArray())))

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
        val google = First.MapData.parseFrom(ByteArrayInputStream(baos.toByteArray()))

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
        assertTrue(MapData.newBuilder().build()
                == MapData.newBuilder().build()) { "two empties are not equals" }
        assertTrue(MapData.defaultValue == MapData.newBuilder().build()) { "two empty and defaultValue are not equals" }
    }

    fun googleValue() = First.MapData.newBuilder()
        .putDoubleValue("DoubleValueKey", 1.0)
        .putFloatValue("FloatValueKey", 2.0F)
        .putInt64Value("Int64ValueKey", 3)
        .putUint64Value("Uint64ValueKey", 4)
        .putFixed64Value("Fixed64ValueKey", 5)
        .putSfixed64Value("Sfixed64ValueKey", 6)
        .putSint64Value("Sint64ValueKey", 7)
        .putInt32Value("Int32ValueKey", 8)
        .putFixed32Value("Fixed32ValueKey", 9)
        .putSfixed32Value("Sfixed32ValueKey", 10)
        .putUint32Value("Uint32ValueKey", 11)
        .putSint32Value("Sint32ValueKey", 12)
        .putBoolValue("BoolValueKey", true)
        .putStringValue("StringValueKey", "hello! this is test string!")
        .putBytesValue("BytesValueKey", ByteString.copyFrom("bytes data".toByteArray()))
        .build()

    fun lightValue() = MapData.newBuilder()
        .putDoubleValue("DoubleValueKey", 1.0)
        .putFloatValue("FloatValueKey", 2.0F)
        .putInt64Value("Int64ValueKey", 3)
        .putUint64Value("Uint64ValueKey", 4)
        .putFixed64Value("Fixed64ValueKey", 5)
        .putSfixed64Value("Sfixed64ValueKey", 6)
        .putSint64Value("Sint64ValueKey", 7)
        .putInt32Value("Int32ValueKey", 8)
        .putFixed32Value("Fixed32ValueKey", 9)
        .putSfixed32Value("Sfixed32ValueKey", 10)
        .putUint32Value("Uint32ValueKey", 11)
        .putSint32Value("Sint32ValueKey", 12)
        .putBoolValue("BoolValueKey", true)
        .putStringValue("StringValueKey", "hello! this is test string!")
        .putBytesValue("BytesValueKey", Bytes.wrapUnsafe("bytes data".toByteArray()))
        .build()
}
