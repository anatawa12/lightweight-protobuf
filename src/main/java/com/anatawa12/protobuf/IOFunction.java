package com.anatawa12.protobuf;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction<T, R> {
    R apply(T t) throws IOException;
}
