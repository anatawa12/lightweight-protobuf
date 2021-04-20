package com.anatawa12.protobuf;

public final class Constants {
    private Constants() {
    }

    @SuppressWarnings({"SpellCheckingInspection"})
    public static final int TYPE_VARINT = 0;
    public static final int TYPE_64BIT = 1;
    public static final int TYPE_DELIMITED = 2;
    public static final int TYPE_START = 3;
    public static final int TYPE_END = 4;
    public static final int TYPE_32BIT = 5;

    public static String typeName(int type) {
        switch (type) {
            case TYPE_VARINT:
                return "VARINT";
            case TYPE_64BIT:
                return "64BIT";
            case TYPE_DELIMITED:
                return "DELIMITED";
            case TYPE_START:
                return "START";
            case TYPE_END:
                return "END";
            case TYPE_32BIT:
                return "32BIT";
            default:
                return "unknown " + type;
        }
    }
}
