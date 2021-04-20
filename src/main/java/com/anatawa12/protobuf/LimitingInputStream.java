package com.anatawa12.protobuf;

import java.io.IOException;
import java.io.InputStream;

class LimitingInputStream extends InputStream {
    final InputStream s;
    long remain;

    public LimitingInputStream(InputStream s, long remain) {
        this.s = s;
        this.remain = remain;
    }

    @Override
    public int read() throws IOException {
        if (remain <= 0) return -1; // eof
        remain--;
        return s.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (remain <= 0) return -1; // eof
        if (len == 0) return 0;
        if (remain < len) len = (int) remain; // limit
        int read = s.read(b, off, len);
        remain -= read;
        return read;
    }

    @Override
    public int available() throws IOException {
        int available = s.available();
        if (remain < available) available = (int) remain;
        return available;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n > remain) n = remain;
        long skipped = s.skip(n);
        remain -= skipped;
        return skipped;
    }

    @Override
    public void close() throws IOException {
        s.close();
    }

    long mark;

    @Override
    public boolean markSupported() {
        return s.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        s.mark(readlimit);
        mark = remain;
    }

    @Override
    public synchronized void reset() throws IOException {
        s.reset();
        remain = mark;
    }
}
