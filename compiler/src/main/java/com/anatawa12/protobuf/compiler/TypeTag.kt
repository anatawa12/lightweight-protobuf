package com.anatawa12.protobuf.compiler

enum class TypeTag(val id: Int) {
    TYPE_VARINT(0),
    TYPE_64BIT(1),
    TYPE_DELIMITED(2),

    //TYPE_START(3),
    //TYPE_END(4),
    TYPE_32BIT(5),
    ;
}
