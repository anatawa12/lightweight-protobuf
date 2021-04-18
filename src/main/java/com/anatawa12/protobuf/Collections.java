package com.anatawa12.protobuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Collections {
    private Collections(){
    }

    public static <T> List<T> makeImmutable(List<T> input) {
        return input == null ? java.util.Collections.emptyList()
                : java.util.Collections.unmodifiableList(new ArrayList<>(input));
    }

    public static <K, V> Map<K, V> makeImmutable(Map<K, V> input) {
        return input == null ? java.util.Collections.emptyMap()
                : java.util.Collections.unmodifiableMap(new HashMap<>(input));
    }
}
