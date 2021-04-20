/*
 * Generated by lists-generator.kts
 */

package com.anatawa12.protobuf;

import java.util.Objects;

@FunctionalInterface
public interface BooleanConsumer {
    void accept(boolean value);

    default BooleanConsumer andThen(BooleanConsumer after) {
        Objects.requireNonNull(after);
        return (boolean t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
