package com.anatawa12.protobuf;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.IntFunction;

import static com.anatawa12.protobuf.Constants.TYPE_32BIT;
import static com.anatawa12.protobuf.Constants.TYPE_64BIT;
import static com.anatawa12.protobuf.Constants.TYPE_DELIMITED;
import static com.anatawa12.protobuf.Constants.TYPE_END;
import static com.anatawa12.protobuf.Constants.TYPE_START;
import static com.anatawa12.protobuf.Constants.TYPE_VARINT;
import static com.anatawa12.protobuf.Constants.typeName;

@SuppressWarnings({"SpellCheckingInspection", "PointlessArithmeticExpression"})
public class WireReader {
    // markSupported is required
    private InputStream s;
    // the full tag. lowest 3 bits are type tag and remains are field Id. zero for EOF.
    // -1 for to be read
    private int tag = -1;

    // the buffer to read s few bytes at once
    private byte[] buf;

    public WireReader(InputStream s) {
        if (s.markSupported())
            this.s = s;
        else
            this.s = new BufferedInputStream(s);
        assert s.markSupported();
    }

    /**
     * the full tag. lowest 3 bits are type tag and remains are field Id. zero for EOF.
     * @return the full tag
     */
    public int tag() throws IOException {
        if (tag == -1) readTagAndId();
        return tag;
    }

    public int fieldId() throws IOException {
        if (tag == -1) readTagAndId();
        return tag >>> 3;
    }

    public int typeTag() throws IOException {
        if (tag == -1) readTagAndId();
        return tag & 0b111;
    }

    private int currentTypeTag() {
        return tag & 0b111;
    }

    // values

    public int varint32() throws IOException {
        checktag(TYPE_VARINT);
        return readVarint32Unsafe();
    }

    public long varint64() throws IOException {
        checktag(TYPE_VARINT);
        return readVarint64Unsafe();
    }

    public int sint32() throws IOException {
        return zigzag32(varint32());
    }

    public long sint64() throws IOException {
        return zigzag64(varint64());
    }

    public boolean bool() throws IOException {
        checktag(TYPE_VARINT);
        return readBoolUnsafe();
    }

    // you need to use varint32 for enum

    public long fixed64() throws IOException {
        checktag(TYPE_64BIT);
        return readFixed64Unsafe();
    }

    // in specification double
    public double float64() throws IOException {
        return Double.longBitsToDouble(fixed64());
    }

    public int fixed32() throws IOException {
        checktag(TYPE_32BIT);
        return readFixed32Unsafe();
    }

    // in specification double
    public float float32() throws IOException {
        return Float.intBitsToFloat(fixed32());
    }

    public <T> T enumValue(IntFunction<T> resolver) throws IOException {
        int tag = varint32();
        T value = resolver.apply(tag);
        if (value == null) throw new ProtocolException("Illegal enum tag: " + tag);
        return value;
    }

    public String string() throws IOException {
        checktag(TYPE_DELIMITED);
        int len = delimited();
        return new String(buf, 0, len, StandardCharsets.UTF_8);
    }

    public Bytes bytes() throws IOException {
        checktag(TYPE_DELIMITED);
        int len = delimited();
        if (buf.length != len) {
            return Bytes.wrapUnsafe(Arrays.copyOf(buf, len));
        } else {
            byte[] r = buf;
            buf = null;
            return Bytes.wrapUnsafe(r);
        }
    }

    public <T> T embedded(IOFunction<WireReader, T> reader) throws IOException {
        EmbeddedMarker marker = startEmbedded();
        try {
            return reader.apply(this);
        } finally {
            endEmbedded(marker);
        }
    }

    public EmbeddedMarker startEmbedded() throws IOException {
        checktag(TYPE_DELIMITED);
        LimitingInputStream newStream = new LimitingInputStream(s, readVarint64Unsafe());
        assert newStream.markSupported();
        s = newStream;
        tag = -1;
        return new EmbeddedMarker(newStream);
    }

    public void endEmbedded(EmbeddedMarker marker) throws IOException {
        long remain = marker.s.remain;
        if (marker.s.skip(remain) != remain)
            throw new ProtocolException("Malformed DELIMITED");
        s = marker.s.s;
        assert s.markSupported();
        tag = -1;
    }

    public void skip() throws IOException {
        if (fieldId() == 0) return;
        switch (typeTag()) {
            case TYPE_VARINT:
                varint();
            case TYPE_64BIT:
                fixed64();
            case TYPE_DELIMITED:
                delimited();
            case TYPE_START:
                skipGroup();
            case TYPE_END:
                throw new ProtocolException("maniformed GROUP");
            case TYPE_32BIT:
                fixed32();
            default:
                throw new ProtocolException("unknwon tag: " + typeTag());
        }
    }

    public static class EmbeddedMarker {
        LimitingInputStream s;

        public EmbeddedMarker(LimitingInputStream s) {
            this.s = s;
        }
    }

    // unsafe operations. DO NOT USE unless you've understood protobuf format

    public boolean readBoolUnsafe() throws IOException {
        return varint() != 0;
    }

    public int readVarint32Unsafe() throws IOException {
        return (int) varint();
    }

    public long readVarint64Unsafe() throws IOException {
        return varint();
    }

    public long readFixed64Unsafe() throws IOException {
        byte[] buf = this.buf == null ? (this.buf = new byte[8]) : this.buf;
        if (s.read(buf, 0, 8) != 8) throw new ProtocolException("Malformed 64BIT");
        tag = -1;
        return (buf[7] & 0xFFL) << (8 * 7)
                | (buf[6] & 0xFFL) << (8 * 6)
                | (buf[5] & 0xFFL) << (8 * 5)
                | (buf[4] & 0xFFL) << (8 * 4)
                | (buf[3] & 0xFFL) << (8 * 3)
                | (buf[2] & 0xFFL) << (8 * 2)
                | (buf[1] & 0xFFL) << (8 * 1)
                | (buf[0] & 0xFFL) << (8 * 0)
                ;
    }

    public int readFixed32Unsafe() throws IOException {
        byte[] buf = this.buf == null ? (this.buf = new byte[8]) : this.buf;
        if (s.read(buf, 0, 4) != 4) throw new ProtocolException("Malformed 32Bit");
        tag = -1;
        return (buf[3] & 0xFF) << (8 * 3)
                | (buf[2] & 0xFF) << (8 * 2)
                | (buf[1] & 0xFF) << (8 * 1)
                | (buf[0] & 0xFF) << (8 * 0)
                ;
    }

    public <T> T readEnumValueUnsafe(IntFunction<T> resolver) throws IOException {
        int tag = readVarint32Unsafe();
        T value = resolver.apply(tag);
        if (value == null) throw new ProtocolException("Illegal enum tag: " + tag);
        return value;
    }

    public boolean hasRemaining() throws IOException {
        int r;
        s.mark(1);
        r = s.read();
        s.reset();
        return r >= 0;
    }

    public static int zigzag32(int raw) {
        return (raw >>> 1) ^ -(raw & 1);
    }

    public static long zigzag64(long raw) {
        return (raw >>> 1) ^ -(raw & 1);
    }

    // internals

    private void readTagAndId() throws IOException {
        while (true) {
            readTagAndIdInternal();
            if (currentTypeTag() == TYPE_START) skip();
            else return;
        }
    }

    private void readTagAndIdInternal() throws IOException {
        if (!hasRemaining()) {
            tag = 0;
            return;
        }
        tag = readVarint32Unsafe();
    }

    private void checktag(int tag) throws IOException {
        if (tag() == 0)
            throw new EOFException();
        // this.tag for skip initializing
        if (currentTypeTag() != tag)
            throw new ProtocolException("Expected " + typeName(tag)
                    + " but was " + typeTag());
    }

    private long varint() throws IOException {
        long r = 0;
        int b;
        int index = 0;
        do {
            b = s.read();
            if (b < 0) throw new ProtocolException("Malformed VARINT");
            r = r | (b & 0x7FL) << index++ * 7;
        } while ((b & 0x80) != 0);
        tag = -1;
        return r;
    }

    private int delimited() throws IOException {
        int length = readVarint32Unsafe();
        if (length < 0) throw new ProtocolException("Malformed DELIMITED: too long for java");
        byte[] buf = this.buf == null || this.buf.length < length
                ? this.buf = new byte[length] : this.buf;
        if (s.read(buf, 0, length) != length)
            throw new ProtocolException("Malformed DELIMITED");
        tag = -1;
        return length;
    }

    private void skipGroup() throws IOException {
        tag = -1;
        while (true) {
            if (fieldId() == 0) throw new EOFException("GROUP");
            if (typeTag() == TYPE_END) {
                tag = -1;
                return;
            } else {
                skip();
            }
        }
    }

    private IntList packed32(ToIntIOFunction<WireReader> reader) throws IOException {
        return embedded((r) -> {
            IntList ints = new IntList();
            while (r.hasRemaining())
                ints.add(reader.apply(this));
            return ints;
        });
    }

    private LongList packed64(ToLongIOFunction<WireReader> reader) throws IOException {
        return embedded((r) -> {
            LongList longs = new LongList();
            while (r.hasRemaining())
                longs.add(reader.apply(this));
            return longs;
        });
    }

    @FunctionalInterface
    private interface ToIntIOFunction<T> {
        int apply(T t) throws IOException;
    }

    @FunctionalInterface
    private interface ToLongIOFunction<T> {
        long apply(T t) throws IOException;
    }
}
