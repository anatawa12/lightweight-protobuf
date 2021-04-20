package com.anatawa12.protobuf;

public final class Objects {
    private Objects() {
    }

    public static int hash(double d) {
        return hash(Double.doubleToLongBits(d));
    }

    public static int hash(float f) {
        return hash(Float.floatToIntBits(f));
    }

    public static int hash(long l) {
        return (int) (l ^ l >>> 32);
    }

    public static int hash(int i) {
        return i;
    }

    public static int hash(boolean b) {
        return b ? 1231 : 1237;
    }

    public static int hash(Object o) {
        return o == null ? 0 : o.hashCode();
    }
}
