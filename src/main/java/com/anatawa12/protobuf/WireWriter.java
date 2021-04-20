package com.anatawa12.protobuf;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.PrimitiveIterator;

/**
 * The class to write in wire format.
 */
public final class WireWriter {
    /**
     * in this buffer,
     * - use fixed32 for length of delimited
     */
    private byte[] buf;
    private int cur;
    // the difference of size.
    // difference will be happened because some varint is put in fixed32 in th buf.
    // positive number means real size is greater than original wire.
    private int sizeDiff;
    private int writingDelimited;
    // the list of values witten in fixed32 in buf.
    private final IntList varintInFixed32Indices = new IntList();

    public WireWriter() {
        this.buf = new byte[256];
        this.cur = 0;
    }

    //region for message serialization method

    /**
     * puts tag to buffer
     */
    public void putTag(int tag) {
        putVarint32(tag);
    }

    public void putBool(boolean value) {
        putVarint32(value ? 1 : 0);
    }

    public void putBytes(Bytes value) {
        putDelimited(value.getBytesUnsafe());
    }

    public void putFloat64(double value) {
        putFixed64(Double.doubleToLongBits(value));
    }

    /**
     * puts fixed32 data.
     */
    public void putFixed32(int value) {
        growAdding(4);
        putFixed32At(cur, value);
        cur += 4;
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    private void putFixed32At(int at, int value) {
        buf[at + 0] = (byte) (value >>> (8 * 0));
        buf[at + 1] = (byte) (value >>> (8 * 1));
        buf[at + 2] = (byte) (value >>> (8 * 2));
        buf[at + 3] = (byte) (value >>> (8 * 3));
    }

    /**
     * puts fixed64 data.
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    public void putFixed64(long value) {
        growAdding(8);
        buf[cur + 0] = (byte) (value >>> (8 * 0));
        buf[cur + 1] = (byte) (value >>> (8 * 1));
        buf[cur + 2] = (byte) (value >>> (8 * 2));
        buf[cur + 3] = (byte) (value >>> (8 * 3));
        buf[cur + 4] = (byte) (value >>> (8 * 4));
        buf[cur + 5] = (byte) (value >>> (8 * 5));
        buf[cur + 6] = (byte) (value >>> (8 * 6));
        buf[cur + 7] = (byte) (value >>> (8 * 7));
        cur += 8;
    }

    public void putFloat32(float value) {
        putFixed32(Float.floatToIntBits(value));
    }

    /**
     * puts int32
     */
    public void putVarint32(int value) {
        putVarint64(value & 0xFFFFFFFFL);
    }

    /**
     * puts int64
     */
    public void putVarint64(long value) {
        growAdding(getByteCountInVarInt(value));
        cur = putVarintTo(value, buf, cur);
    }

    private static int putVarintTo(long value, byte[] buf, int cur) {
        do {
            buf[cur++] = (byte) (0x80 | value & 0x7F);
            value >>>= 7;
        } while (value != 0);
        // for last byte, remove highest bit.
        buf[cur - 1] &= 0x7F;
        return cur;
    }

    private int getByteCountInVarInt(long value) {
        return (Numbers.msb(value) / 7 + 1);
    }

    public void putSint32(int value) {
        putVarint64(zigzag(value));
    }

    public void putSint64(long value) {
        putVarint64(zigzag(value));
    }

    public static long zigzag(long value) {
        return (value << 1) ^ (value >> 63);
    }

    public void putString(String value) {
        putDelimited(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * puts byte array data as a delimited.
     * this puts length and data.
     */
    public void putDelimited(byte[] data) {
        varintInFixed32Indices.add(cur);
        putFixed32(data.length);
        sizeDiff += getByteCountInVarInt(data.length & 0xFFFFFFFFL) - 4;
        putBytes(data);
    }

    public class DelimitedTag {
        private DelimitedTag() {
        }

        // the position of size field.
        private int sizePos;
        private int writingDelimited;
        private int sizeDiffAtStart;

        private WireWriter getOuter() {
            return WireWriter.this;
        }
    }

    /**
     * begins delimited. this does not puts tag so please use {@link WireWriter#putTag putTag}
     * to put tag.
     */
    public DelimitedTag startDelimited() {
        growAdding(4);
        DelimitedTag tag = new DelimitedTag();
        tag.sizePos = cur;
        tag.writingDelimited = ++writingDelimited;
        tag.sizeDiffAtStart = sizeDiff;
        varintInFixed32Indices.add(cur);
        // skip 4 bytes for size field
        cur += 4;
        return tag;
    }

    public void endDelimited(DelimitedTag tag) {
        if (tag.getOuter() != this)
            throw new IllegalArgumentException("the DelimitedTag is not of this");
        if (tag.writingDelimited == -1)
            throw new IllegalArgumentException("the DelimitedTag is already end");
        if (tag.writingDelimited != writingDelimited)
            throw new IllegalStateException("there's inner writing delimited");
        writingDelimited--;
        int size = cur - (tag.sizePos + 4)
                + (sizeDiff - tag.sizeDiffAtStart);
        tag.writingDelimited = -1;
        putFixed32At(tag.sizePos, size);
        sizeDiff += getByteCountInVarInt(size & 0xFFFFFFFFL) - 4;
    }

    public void putEnumValue(int value) {
        putVarint32(value);
    }

    //endregion

    //region serialization utils
    private void growAdding(int size) {
        if (size < 0) throw new OutOfMemoryError("too big to grow");
        grow(cur + size);
    }

    private void grow(final int request) {
        if (request < 0)
            throw new OutOfMemoryError("too big to grow");
        int newSize = buf.length;
        while (newSize > 0 && newSize < request) newSize *= 2;
        // overflow: set to to MAX_VALUE.
        if (newSize <= 0) newSize = Integer.MAX_VALUE;
        //noinspection ConditionalBreakInInfiniteLoop
        while (true) {
            // if glowing to less than current buffer size, it means no-op
            if (newSize <= buf.length) return;
            try {
                buf = Arrays.copyOf(buf, newSize);
                break;
            } catch (OutOfMemoryError error) {
                // if OOME because of MV limit, cut down the size of array
                // if OOME because of no memory, "Java heap space" will be thrown
                if (error.getMessage().contains("Requested array size exceeds VM limit")) {
                    newSize--;
                    if (newSize < request) throw error;
                    continue;
                } else if (request < newSize) {
                    // if allocating too big, try with as small as possible.
                    newSize = request;
                    continue;
                }
                throw error;
            }
        }
    }

    private void putBytes(byte[] data) {
        growAdding(data.length);
        System.arraycopy(
                data, 0,
                buf, cur,
                data.length);
        cur += data.length;
    }
    // endregion

    //region export
    public void writeTo(OutputStream stream) throws IOException {
        checkReadyToWrite();
        int start = 0;
        PrimitiveIterator.OfInt iter = varintInFixed32Indices.iterator();
        if (iter.hasNext()) {
            byte[] internalBuf = new byte[getByteCountInVarInt(0xFFFFFFFF)];
            do {
                int end = iter.next();
                stream.write(buf, start, end - start);
                //noinspection PointlessArithmeticExpression
                int value = (buf[end + 3] & 0xFF) << (8 * 3)
                        | (buf[end + 2] & 0xFF) << (8 * 2)
                        | (buf[end + 1] & 0xFF) << (8 * 1)
                        | (buf[end + 0] & 0xFF) << (8 * 0);
                int varintLen = putVarintTo(value, internalBuf, 0);
                stream.write(internalBuf, 0, varintLen);
                start = end + 4;
            } while (iter.hasNext());
        }
        stream.write(buf, start, cur - start);
    }

    private void checkReadyToWrite() {
        if (writingDelimited != 0)
            throw new IllegalStateException("there's some writing delimited");
    }
    //endregion
}
