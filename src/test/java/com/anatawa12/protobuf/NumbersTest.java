package com.anatawa12.protobuf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumbersTest {
    @Test
    void msb() {
        assertEquals(0, Numbers.msb(0));
        assertEquals(0, Numbers.msb(1));
        assertEquals(63, Numbers.msb(0x8000000000000000L));
        assertEquals(63, Numbers.msb(0xF000000000000000L));
    }
}
