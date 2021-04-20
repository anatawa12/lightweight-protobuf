package com.anatawa12.protobuf;

public final class Numbers {
    private Numbers() {
    }

    private static final byte[] hash2pos;

    static {
        hash2pos = new byte[64];
        long hash = 0x03F566ED27179461L;
        for ( byte i = 0; i < 64; i++ ) {
            hash2pos[(int)((hash << i) >>> 58)] = i;
        }
    }

    /**
     * returns position of most significant bit.
     * for 0, returns 0. for 1, returns 0. for 1 << 63, returns 63;
     */
    public static int msb(long x) {
        // keep only most significant bit.
        x |= (x >>> 1);
        x |= (x >>> 2);
        x |= (x >>> 4);
        x |= (x >>> 8);
        x |= (x >>> 16);
        x |= (x >>> 32);
        // ^^ fill bit since 0 until msb
        x = x ^ (x >>> 1);
        // ^^ keep only msb
        return hash2pos[(int) ((x * 0x03F566ED27179461L) >>> 58)];
    }
}
