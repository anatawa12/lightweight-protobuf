package com.anatawa12.protobuf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WireBufferTest {
    @Test
    public void zigzag() {
        assertEquals(0, WireWriter.zigzag(0));
        assertEquals(1, WireWriter.zigzag(-1));
        assertEquals(2, WireWriter.zigzag(+1));
        assertEquals(3, WireWriter.zigzag(-2));
        assertEquals(4294967294L, WireWriter.zigzag(+2147483647));
        assertEquals(4294967295L, WireWriter.zigzag(-2147483648));
    }
}
