package com.anatawa12.protobuf;

import java.io.IOException;
import java.io.OutputStream;

public abstract class Message {
    public void writeTo(OutputStream stream) throws IOException {
        WireWriter writer = new WireWriter();
        writeTo(writer);
        writer.writeTo(stream);
    }

    public abstract void writeTo(WireWriter writer);
}
