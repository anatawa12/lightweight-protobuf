package com.anatawa12.protobuf

import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

internal class WireReaderTest {
    @Test
    fun test() {
        val r = WireReader(ByteArrayInputStream(byteArrayOf(2, 0)))
        r.embedded {

        }
    }
}
