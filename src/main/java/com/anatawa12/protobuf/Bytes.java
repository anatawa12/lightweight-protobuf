package com.anatawa12.protobuf;

import java.util.Arrays;

public class Bytes {
    private final byte[] bytes;

    private Bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public static Bytes copyOf(byte[] bytes) {
        return new Bytes(Arrays.copyOf(bytes, bytes.length));
    }

    public static Bytes copyOf(Bytes bytes) {
        return new Bytes(bytes.getBytesUnsafe());
    }

    public static Bytes wrapUnsafe(byte[] bytes) {
        return new Bytes(bytes);
    }

    /**
     * @return copy of content
     */
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * @return content. DO NOT MODIFY RETURNED ARRAY
     */
    public byte[] getBytesUnsafe() {
        return bytes;
    }

    public int length() {
        return bytes.length;
    }

    public byte get(int index) {
        return bytes[index];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(bytes.length * 2 + 7);
        builder.append("Bytes(");
        for (byte aByte : bytes) {
            builder.append(hex[aByte >> 4 & 0xF]);
            builder.append(hex[aByte & 0xF]);
        }
        builder.append(')');
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return Arrays.equals(bytes, ((Bytes) o).bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    private static final char[] hex = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static final Bytes defaultValue = new Bytes(new byte[0]);
}
