package com.anatawa12.protobuf.compiler

object MessageBuilder {
    fun generateMessage(msg: MessageInfo, isRoot: Boolean = true): Source = buildSource {
        val static = if (isRoot) "" else " static"

        block("public$static final class ${msg.fqName.simpleName} extends $protobuf.Message") {

            +generateMessageBody(msg)
            +""
            +generateMessageBuilder(msg)

            for (nestedType in msg.nestedTypes.values) {
                +""
                when (nestedType) {
                    is MessageInfo -> {
                        // mapEntry will never be used
                        if (nestedType.real.options.mapEntry) continue
                        +generateMessage(nestedType, false)
                    }
                    is EnumInfo -> {
                        +EnumBuilder.generateEnum(nestedType)
                    }
                }
            }
        }
    }

    private fun generateMessageBody(msg: MessageInfo): Source = buildSource {
        +"public static final ${msg.javaName} defaultValue = newBuilder().build();"
        +""
        generateFieldsAndGetters(msg)
        block("${msg.fqName.simpleName}(Builder builder)") {
            // generate fields
            for (field in msg.fields) {
                if (!field.oneOf) {
                    if (field.type is CollectionTypeInfo) {
                        +"this.field${field.number} = ${field.type.newImmutable("builder.field${field.number}")};"
                    } else {
                        +"this.field${field.number} = builder.field${field.number};"
                    }
                } else {
                    block("if (builder.${v("has", field.name)}())") {
                        if (field.type is CollectionTypeInfo) {
                            +"this.oneOf${field.index} = ${
                                field.type.newImmutable("builder.${
                                    v("get",
                                        field.name)
                                }()")
                            };"
                        } else {
                            +"this.oneOf${field.index} = builder.${v("get", field.name)}();"
                        }
                        +"this.oneOfStat${field.index} = ${field.number};"
                    }
                }
            }
        }
        +""
        generateToString(msg)
        +""
        generateEqualsAndHashCode(msg, msg.fqName.simpleName)
        +""
        block("public static Builder newBuilder()") {
            +"return new Builder();"
        }
        +""
        +generateRead(msg)
        +""
        generateWrite(msg)
    }

    private fun generateMessageBuilder(msg: MessageInfo): Source = buildSource("public static final class Builder") {
        generateFieldsAndGetters(msg, builder = true)
        +generateBuilderBody(msg)
        block("Builder()") {
            for (field in msg.fields) {
                if (!field.oneOf) {
                    when (field.type) {
                        //is MapTypeInfo,
                        //is RepeatedTypeInfo,
                        PrimitiveType.String,
                        is EnumInfo,
                        -> +"this.field${field.number} = ${field.type.defaultValue};"
                        else -> Unit
                    }
                }
            }
        }
        +""
        block("public ${msg.javaName} build()") {
            +"return new ${msg.javaName}(this);"
        }
    }

    private fun SourceBuilder.generateToString(msg: MessageInfo) {
        +"@java.lang.Override"
        block("public final java.lang.String toString()") {
            if (msg.fields.isEmpty()) {
                +"return \"${msg.fqName.simpleName}{}\";"
            } else {
                +"java.lang.StringBuilder builder = new java.lang.StringBuilder(\"${msg.fqName.simpleName}{\");"
                +"boolean first = true;"
                +"// fields"
                for (field in msg.fields) {
                    if (field.oneOf) {
                        block("if (${v("has", field.name)}())") {
                            +"if (!first) builder.append(\", \");"
                            +"first = false;"
                            +"builder.append(\"${field.name} = \").append(this.oneOf${field.index});"
                        }
                    } else if (field.type == PrimitiveType.Bool) {
                        block("if (this.field${field.number} != false)") {
                            +"if (!first) builder.append(\", \");"
                            +"first = false;"
                            +"builder.append(\"${field.name} = true\");"
                        }
                    } else {
                        val checkExisting = when (field.type) {
                            is MapTypeInfo -> "!this.field${field.number}.isEmpty()"
                            is RepeatedTypeInfo -> "!this.field${field.number}.isEmpty()"
                            is PrimitiveType -> checkExistingForPrimitive(field.type, "this.field${field.number}")
                            is EnumInfo -> "this.field${field.number}.getId() != 0"
                            is MessageInfo -> "${v("has", field.name)}()"
                        }
                        block("if (${checkExisting})") {
                            +"if (!first) builder.append(\", \");"
                            +"first = false;"
                            +"builder.append(\"${field.name} = \").append(this.field${field.number});"
                        }
                    }
                }
                +"return builder.append('}').toString();"
            }
        }
    }

    private fun checkExistingForPrimitive(type: PrimitiveType, getter: String) = when (type) {
        PrimitiveType.String -> "!$getter.isEmpty()"
        PrimitiveType.Bool -> "$getter != false"
        PrimitiveType.Bytes -> "$getter.length() != 0"
        PrimitiveType.Double -> "$getter != 0"
        PrimitiveType.Fixed32 -> "$getter != 0"
        PrimitiveType.Fixed64 -> "$getter != 0"
        PrimitiveType.Float -> "$getter != 0"
        PrimitiveType.Int32 -> "$getter != 0"
        PrimitiveType.Int64 -> "$getter != 0"
        PrimitiveType.SFixed32 -> "$getter != 0"
        PrimitiveType.SFixed64 -> "$getter != 0"
        PrimitiveType.SInt32 -> "$getter != 0"
        PrimitiveType.SInt64 -> "$getter != 0"
        PrimitiveType.Uint32 -> "$getter != 0"
        PrimitiveType.Uint64 -> "$getter != 0"
    }

    private fun SourceBuilder.generateEqualsAndHashCode(msg: MessageInfo, thisName: String) {
        +"@java.lang.Override"
        block("public final int hashCode()") {
            +"int hash = 0;"
            +"// fields"
            for (field in msg.fields) {
                if (!field.oneOf) {
                    if (field.type is MessageInfo) {
                        block("if (${v("has", field.name)}())") {
                            +"hash = hash * 31 + $protobuf.Objects.hash(this.field${field.number});"
                        }
                    } else {
                        +"hash = hash * 31 + $protobuf.Objects.hash(this.field${field.number});"
                    }
                }
            }
            +"// oneof"
            for (index in msg.oneofNameList.indices) {
                +"hash = hash * 31 + $protobuf.Objects.hash(this.oneOf$index);"
                +"hash = hash * 31 + this.oneOfStat$index;"
            }
            +"return hash;"
        }
        +""
        +"@java.lang.Override"
        block("public final boolean equals(Object other)") {
            +"return this == other || other != null && other.getClass() == this.getClass() && equals(($thisName)other);"
        }
        +""
        block("public final boolean equals($thisName other)") {
            +"if (this == other) return true;"
            +"// fields"
            for (field in msg.fields) {
                if (!field.oneOf) {
                    when {
                        field.type.javaName == "double" -> {
                            +("if (Double.doubleToLongBits(this.${v("get", field.name)}()) " +
                                    "!= Double.doubleToLongBits(other.${v("get", field.name)}())) return false;")
                        }
                        field.type.javaName == "float" -> {
                            +("if (Float.floatToIntBits(this.${v("get", field.name)}()) " +
                                    "!= Float.floatToIntBits(other.${v("get", field.name)}())) return false;")
                        }
                        field.type.isJavaPrimitive() -> {
                            +"if (this.${v("get", field.name)}() != other.${v("get", field.name)}()) return false;"
                        }
                        else -> {
                            +("if (!java.util.Objects.equals(this.${v("get", field.name)}(), " +
                                    "other.${v("get", field.name)}())) return false;")
                        }
                    }
                }
            }
            +"// oneof"
            for (index in msg.oneofNameList.indices) {
                +"if (this.oneOfStat$index != other.oneOfStat$index) return false;"
                +"if (!java.util.Objects.equals(this.oneOf$index, other.oneOf$index)) return false;"
            }
            +"return true;"
        }
    }

    private fun generateBuilderBody(msg: MessageInfo): Source = buildSource {
        // generate fields
        for (field in msg.fields) {
            if (field.oneOf) {
                +"// field ${field.type} ${field.name} = ${field.number} in oneof #${field.index}"
            } else {
                +"// field ${field.type} ${field.name} = ${field.number}"
            }
            if (field.type is CollectionTypeInfo) {
                val notExists = if (field.oneOf) "oneOfStat${field.index} != ${field.number}"
                else "field${field.number} == null"

                +"@java.lang.SuppressWarnings(\"UNCHECKED_CAST\")"
                block("private ${field.type.javaName} make${field.number}()") {
                    +"${field.type.javaName} value = ${v("get", field.name)}();"
                    block("if ($notExists)") {
                        +"value = ${field.type.newValue};"
                        +"${v("set", field.name)}(value);"
                    }
                    +"return value;"
                }
            }
            if (field.oneOf) {
                // oneof field
                block("public Builder ${v("set", field.name)}(${field.type.javaName} value)") {
                    +"oneOf${field.index} = value;"
                    +"oneOfStat${field.index} = ${field.number};"
                    +"return this;"
                }
                +""
            } else {
                block("public Builder ${v("set", field.name)}(${field.type.javaName} value)") {
                    +"field${field.number} = value;"
                    +"return this;"
                }
                +""
            }

            when (field.type) {
                is RepeatedTypeInfo -> {
                    // repeated field
                    if (field.type.element.isJavaPrimitive()) {
                        block("public Builder ${v("addAll", field.name)}(${field.type.element.javaName}[] values)") {
                            +"make${field.number}().addAll(values);"
                            +"return this;"
                        }
                        +""
                    }
                    block("public Builder ${v("addAll", field.name)}(${field.type.javaName} values)") {
                        +"make${field.number}().addAll(values);"
                        +"return this;"
                    }
                    +""
                    block("public Builder ${v("add", field.name)}(${field.type.element.javaName} value)") {
                        +"make${field.number}().add(value);"
                        +"return this;"
                    }
                    +""
                }
                is MapTypeInfo -> {
                    // repeated field
                    block("public Builder ${v("putAll", field.name)}(${field.type.javaName} values)") {
                        +"make${field.number}().putAll(values);"
                        +"return this;"
                    }
                    +""
                    block("public Builder ${v("put", field.name)}" +
                            "(${field.type.key.javaName} key, ${field.type.value.javaName} value)") {
                        +"make${field.number}().put(key, value);"
                        +"return this;"
                    }
                    +""
                }
                else -> Unit
            }
        }
    }

    private fun SourceBuilder.generateFieldsAndGetters(msg: MessageInfo, builder: Boolean = false) {
        // generate fields
        for (field in msg.fields) {
            if (field.oneOf) {
                +"// field ${field.type.javaName} ${field.name} = ${field.number} in oneof #${field.index}"
                // oneof field
                +"@java.lang.SuppressWarnings(\"UNCHECKED_CAST\")"
                block("public final ${field.type.javaName} ${v("get", field.name)}()") {
                    +("return ${v("has", field.name)}() ? " +
                            "(${field.type.javaName}) oneOf${field.index} : ${field.type.defaultValue};")
                }
                +""
                block("public final boolean ${v("has", field.name)}()") {
                    +"return oneOfStat${field.index} == ${field.number};"
                }
                +""
            } else {
                +"// field ${field.type.javaName} ${field.name} = ${field.number}"
                +"${field.type.javaName} field${field.number};"
                +""

                if (builder && field.type is CollectionTypeInfo) {
                    block("public final ${field.type.javaName} ${v("get", field.name)}()") {
                        +"return field${field.number} != null ? field${field.number} : ${field.type.defaultValue};"
                    }
                    +""
                } else if (field.type is MessageInfo) {
                    block("public final ${field.type.javaName} ${v("get", field.name)}()") {
                        +"return field${field.number} != null ? field${field.number} : ${field.type.defaultValue};"
                    }
                    +""
                    block("public final boolean ${v("has", field.name)}()") {
                        +"return field${field.number} != null;"
                    }
                    +""
                } else {
                    block("public final ${field.type.javaName} ${v("get", field.name)}()") {
                        +"return field${field.number};"
                    }
                    +""
                }
            }
        }

        // generate oneofs
        for ((index, oneof) in msg.oneofNameList.withIndex()) {
            +"// oneof $oneof"
            +"Object oneOf$index;"
            +"int oneOfStat$index;"
            +""
        }
    }

    private fun generateRead(msg: MessageInfo): Source = buildSource {
        block("public static ${msg.javaName} parseFrom($protobuf.WireReader reader) throws java.io.IOException") {
            +"Builder builder = newBuilder();"
            block("fields: while (true)") {
                block("switch (reader.tag())") {
                    +generateReadAFieldSwitch(msg)
                    +"case 0: break fields;"
                    +"default: reader.skip(); break;"
                }
            }
            +"return builder.build();"
        }
    }

    private val packable = setOf(TypeTag.TYPE_VARINT, TypeTag.TYPE_64BIT, TypeTag.TYPE_32BIT)

    // generate switch body
    private fun generateReadAFieldSwitch(msg: MessageInfo): Source = buildSource {
        // generate fields
        for (field in msg.fields) {
            when (val type = field.type) {
                is SimpleTypeInfo -> {
                    block("case ${tagOf(field.number, type)}:") {
                        +"builder.${v("set", field.name)}(${generateReadPrimitiveType(type)});"
                        +"break;"
                    }
                }
                is MapTypeInfo -> {
                    block("case ${tagOf(field.number, TypeTag.TYPE_DELIMITED)}:") {
                        +"${type.key.javaName} key = ${type.key.defaultValue};"
                        +"${type.value.javaName} value = ${type.value.defaultValue};"
                        +"$protobuf.WireReader.EmbeddedMarker marker = reader.startEmbedded();"
                        block("try") {
                            block("mapEntry: while(true)") {
                                block("switch(reader.tag())") {
                                    +("case ${tagOf(1, type.key)}: " +
                                            "key = ${generateReadPrimitiveType(type.key)}; break;")
                                    +("case ${tagOf(2, type.value)}: " +
                                            "value = ${generateReadPrimitiveType(type.value)}; break;")
                                    +"case 0: break mapEntry;"
                                    +"default: reader.skip();"
                                }
                            }
                        }
                        block("finally") {
                            +"reader.endEmbedded(marker);"
                        }
                        +"builder.${v("put", field.name)}(key, value);"
                        +"break;"
                    }
                }
                is RepeatedTypeInfo -> {
                    if (type.element.typeTag in packable) {
                        block("case ${tagOf(field.number, TypeTag.TYPE_DELIMITED)}:") {
                            +"$protobuf.WireReader.EmbeddedMarker marker = reader.startEmbedded();"
                            block("try") {
                                block("while (reader.hasRemaining())") {
                                    +("builder.${v("add", field.name)}(" +
                                            "${generateReadAPackedPrimitiveType(type.element)});")
                                }
                            }
                            block("finally") {
                                +"reader.endEmbedded(marker);"
                            }
                            +"break;"
                        }
                    }
                    block("case ${tagOf(field.number, type.element)}:") {
                        +"builder.${v("add", field.name)}(${generateReadPrimitiveType(type.element)});"
                        +"break;"
                    }
                }
            }
        }
    }

    private fun tagOf(number: Int, type: SimpleTypeInfo) = tagOf(number, type.typeTag)

    private fun tagOf(number: Int, type: TypeTag) = "($number << 3) | $protobuf.Constants.$type"

    private fun generateReadAPackedPrimitiveType(type: SimpleTypeInfo): String {
        return when (type) {
            PrimitiveType.Bool -> "reader.readBoolUnsafe()"
            PrimitiveType.Double -> "java.lang.Double.longBitsToDouble(reader.readFixed64Unsafe())"
            PrimitiveType.Fixed32 -> "reader.readFixed32Unsafe()"
            PrimitiveType.Fixed64 -> "reader.readFixed64Unsafe()"
            PrimitiveType.SFixed32 -> "reader.readFixed32Unsafe()"
            PrimitiveType.SFixed64 -> "reader.readFixed64Unsafe()"
            PrimitiveType.Float -> "java.lang.Float.intBitsToFloat(reader.readFixed32Unsafe())"
            PrimitiveType.Int32 -> "reader.readVarint32Unsafe()"
            PrimitiveType.Int64 -> "reader.readVarint64Unsafe()"
            PrimitiveType.Uint32 -> "reader.readVarint32Unsafe()"
            PrimitiveType.Uint64 -> "reader.readVarint64Unsafe()"
            PrimitiveType.SInt32 -> "$protobuf.WireReader.zigzag32(reader.readVarint32Unsafe())"
            PrimitiveType.SInt64 -> "$protobuf.WireReader.zigzag64(reader.readVarint64Unsafe())"
            is EnumInfo -> "reader.readEnumValueUnsafe(${type.javaName}::fromId)"
            else -> error("can't use packed format: $type")
        }
    }

    private fun generateReadPrimitiveType(type: SimpleTypeInfo): String {
        return when (type) {
            PrimitiveType.Bool -> "reader.bool()"
            PrimitiveType.Bytes -> "reader.bytes()"
            PrimitiveType.Double -> "reader.float64()"
            PrimitiveType.Fixed32 -> "reader.fixed32()"
            PrimitiveType.Fixed64 -> "reader.fixed64()"
            PrimitiveType.SFixed32 -> "reader.fixed32()"
            PrimitiveType.SFixed64 -> "reader.fixed64()"
            PrimitiveType.Float -> "reader.float32()"
            PrimitiveType.Int32 -> "reader.varint32()"
            PrimitiveType.Int64 -> "reader.varint64()"
            PrimitiveType.Uint32 -> "reader.varint32()"
            PrimitiveType.Uint64 -> "reader.varint64()"
            PrimitiveType.SInt32 -> "reader.sint32()"
            PrimitiveType.SInt64 -> "reader.sint64()"
            PrimitiveType.String -> "reader.string()"
            is MessageInfo -> "reader.embedded(${type.javaName}::parseFrom)"
            is EnumInfo -> "reader.enumValue(${type.javaName}::fromId)"
        }
    }

    private fun SourceBuilder.generateWrite(msg: MessageInfo) {
        +"@java.lang.Override"
        block("public void writeTo($protobuf.WireWriter writer)") {
            generateWriteBody(msg)
        }
    }

    private fun SourceBuilder.generateWriteAField(
        type: TypeInfo,
        number: Int,
        getter: String,
        packed: Boolean = false,
    ) {
        when (type) {
            is SimpleTypeInfo -> {
                val cond = when (type) {
                    is PrimitiveType -> checkExistingForPrimitive(type, getter)
                    is MessageInfo -> "!java.util.Objects.equals($getter, ${type.defaultValue})"
                    is EnumInfo -> "$getter.getId() != 0"
                }
                block("if ($cond)") {
                    +"writer.putTag(${tagOf(number, type)});"
                    generateWriteSimpleFieldBody(type, getter)
                }
            }
            is MapTypeInfo -> {
                block("for (java.util.Map.Entry${type.genericPart} entry : $getter.entrySet())") {
                    +"writer.putTag(${tagOf(number, TypeTag.TYPE_DELIMITED)});"
                    +"$protobuf.WireWriter.DelimitedTag mapTag = writer.startDelimited();"
                    generateWriteAField(type.key, 1, "entry.getKey()")
                    generateWriteAField(type.value, 2, "entry.getValue()")
                    +"writer.endDelimited(mapTag);"
                }
            }
            is RepeatedTypeInfo -> {
                val elem = type.element
                if (elem.isJavaPrimitive()) {
                    elem as PrimitiveType
                    if (packed) {
                        // write in packed format
                        +"writer.putTag(${tagOf(number, TypeTag.TYPE_DELIMITED)});"
                        +"$protobuf.WireWriter.DelimitedTag mapTag = writer.startDelimited();"
                        +"${elem.primitiveIteratorType} iter = $getter.iterator();"
                        block("while (iter.hasNext())") {
                            +"${elem.javaName} value = iter.${elem.primitiveIteratorNextFunc}();"
                            generateWritePrimitiveType(elem, "value")
                        }
                        +"writer.endDelimited(mapTag);"
                    } else {
                        +"${elem.primitiveIteratorType} iter = $getter.iterator();"
                        block("while (iter.hasNext())") {
                            +"${elem.javaName} value = iter.${elem.primitiveIteratorNextFunc}();"
                            +"writer.putTag(${tagOf(number, elem)});"
                            generateWriteSimpleFieldBody(elem, "value")
                        }
                    }
                } else {
                    +"java.util.Iterator<${elem.javaName}> iter = $getter.iterator();"
                    block("while (iter.hasNext())") {
                        +"${elem.javaName} value = iter.next();"
                        +"writer.putTag(${tagOf(number, elem)});"
                        generateWriteSimpleFieldBody(elem, "value")
                    }
                }
            }
        }
    }

    private fun SourceBuilder.generateWriteSimpleFieldBody(type: SimpleTypeInfo, getter: String) {
        when (type) {
            is PrimitiveType -> {
                generateWritePrimitiveType(type, getter)
            }
            is MessageInfo -> {
                assert(type.typeTag == TypeTag.TYPE_DELIMITED)
                +"$protobuf.WireWriter.DelimitedTag tag = writer.startDelimited();"
                +"$getter.writeTo(writer);"
                +"writer.endDelimited(tag);"
            }
            is EnumInfo -> {
                assert(type.typeTag == TypeTag.TYPE_VARINT)
                +"writer.putEnumValue($getter.getId());"
            }
        }
    }

    // generate switch body
    private fun SourceBuilder.generateWriteBody(msg: MessageInfo) {
        +"// fields"
        for (field in msg.fields) {
            if (!field.oneOf) {
                block("") {
                    generateWriteAField(field.type, field.number, "this.${v("get", field.name)}()", field.packed)
                }
            }
        }
        if (msg.oneofNameList.isEmpty()) return
        val fieldById = msg.fields.asSequence().filter { it.oneOf }.groupBy { it.index }
        //System.err.println("fieldById: $fieldById")
        //System.exit(-1);
        +"// oneof"
        for ((index, oneof) in msg.oneofNameList.withIndex()) {
            val oneofBody = fieldById.getValue(index)
            block("switch (this.oneOfStat$index)") {
                for (field in oneofBody) {
                    block("case ${field.number}:") {
                        +"${field.type.javaName} value = (${field.type.javaName}) this.oneOf$index;"
                        generateWriteAField(field.type as SimpleTypeInfo, field.number, "value")
                        +"break;"
                    }
                }
                block("case 0:") {
                    +"// this means no value"
                }
                block("default:") {
                    +"throw new java.lang.AssertionError(\"invalid oneof state: field#\" + this.oneOfStat$index + \" is not a element of oneof $oneof\");"
                }
            }
        }
    }


    private fun SourceBuilder.generateWritePrimitiveType(type: PrimitiveType, value: String) {
        when (type) {
            PrimitiveType.Bool -> +"writer.putBool($value);"
            PrimitiveType.Bytes -> +"writer.putBytes($value);"
            PrimitiveType.Double -> +"writer.putFloat64($value);"
            PrimitiveType.Fixed32 -> +"writer.putFixed32($value);"
            PrimitiveType.Fixed64 -> +"writer.putFixed64($value);"
            PrimitiveType.SFixed32 -> +"writer.putFixed32($value);"
            PrimitiveType.SFixed64 -> +"writer.putFixed64($value);"
            PrimitiveType.Float -> +"writer.putFloat32($value);"
            PrimitiveType.Int32 -> +"writer.putVarint32($value);"
            PrimitiveType.Int64 -> +"writer.putVarint64($value);"
            PrimitiveType.Uint32 -> +"writer.putVarint32($value);"
            PrimitiveType.Uint64 -> +"writer.putVarint64($value);"
            PrimitiveType.SInt32 -> +"writer.putSint32($value);"
            PrimitiveType.SInt64 -> +"writer.putSint64($value);"
            PrimitiveType.String -> +"writer.putString($value);"
        }
    }

    private fun v(verb: String, name: String) =
        verb + name.split('_', '-').joinToString { it[0].toUpperCase() + it.substring(1) }
}
