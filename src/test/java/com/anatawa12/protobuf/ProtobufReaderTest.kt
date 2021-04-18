package com.anatawa12.protobuf

import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal class ProtobufReaderTest {
    @Test
    fun test() {
        val r = ProtobufReader(ByteArrayInputStream(byteArrayOf(2, 0)))
        r.embedded {

        }
    }
}
