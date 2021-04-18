package com.anatawa12.protobuf.compiler

import com.anatawa12.protobuf.compiler.Options.javaPackage
import com.anatawa12.protobuf.compiler.Types.getWrappedType
import com.google.protobuf.DescriptorProtos
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type

const val protobuf = "com.anatawa12.protobuf"

fun collectTypes(
    file: DescriptorProtos.FileDescriptorProto,
): ProtobufFile {
    val map = mutableMapOf<FqName, UserTypeInfo>()
    val packageName = FqName.from(if (file.hasPackage() && file.`package` != "") ".${file.`package`}" else "")
    val javaPackage = when {
        file.options.hasExtension(javaPackage) -> file.options.getExtension(javaPackage)
        file.options.hasJavaPackage() -> file.options.javaPackage
        else -> null
    }
    val rootTypes = mutableSetOf<UserTypeInfo>()
    for (msg in file.messageTypeList) {
        rootTypes += collectTypesOnMessage(packageName, javaPackage, msg, map)
    }
    for (enum in file.enumTypeList) {
        rootTypes += collectTypesOnEnum(packageName, javaPackage, enum, map)
    }
    return ProtobufFile(
        map,
        rootTypes,
        file,
    )
}

fun collectTypesOnMessage(
    outerName: FqName,
    javaPackage: String?,
    msg: DescriptorProtos.DescriptorProto,
    map: MutableMap<FqName, UserTypeInfo>,
): MessageInfo {
    val fqName = outerName.resolve(msg.name)
    val info = MessageInfo(fqName, javaPackage, msg)
    map[fqName] = info
    val nestedTypes = mutableMapOf<String, UserTypeInfo>()

    for (nestedMessage in msg.nestedTypeList) {
        nestedTypes[nestedMessage.name] = collectTypesOnMessage(fqName, javaPackage, nestedMessage, map)
    }

    for (nestedEnum in msg.enumTypeList) {
        nestedTypes[nestedEnum.name] = collectTypesOnEnum(fqName, javaPackage, nestedEnum, map)
    }

    info.nestedTypes = nestedTypes
    return info
}

fun collectTypesOnEnum(
    outerName: FqName,
    javaPackage: String?,
    enum: DescriptorProtos.EnumDescriptorProto,
    map: MutableMap<FqName, UserTypeInfo>,
): EnumInfo {
    val fqName = outerName.resolve(enum.name)
    val info = EnumInfo(fqName, javaPackage, enum)
    map[fqName] = info
    return info
}

class ProtobufFile(
    val types: Map<FqName, UserTypeInfo>,
    val rootTypes: Set<UserTypeInfo>,
    val real: DescriptorProtos.FileDescriptorProto,
) {
}

sealed class TypeInfo {
    abstract val javaName: String
    abstract val defaultValue: String
    abstract override fun toString(): String
    abstract fun isJavaPrimitive(): Boolean

    //abstract val javaType: String
    companion object {
        fun of(field: DescriptorProtos.FieldDescriptorProto, types: Map<FqName, UserTypeInfo>): TypeInfo {
            var type: TypeInfo
            type = SimpleTypeInfo.of(field, types)
            if (type is MessageInfo && type.real.options.mapEntry) {
                /*
                For maps fields:
                    map<KeyType, ValueType> map_field = 1;
                The parsed descriptor looks like:
                    message MapFieldEntry {
                        option map_entry = true;
                        optional KeyType key = 1;
                        optional ValueType value = 2;
                    }
                    repeated MapFieldEntry map_field = 1;
                 */
                type = MapTypeInfo(of(type.real.fieldList.first { it.number == 1 }, types) as PrimitiveType,
                    of(type.real.fieldList.first { it.number == 2 }, types) as SimpleTypeInfo)
            } else if (field.label == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                type = RepeatedTypeInfo(type)
            }
            return type
        }
    }
}

sealed class CollectionTypeInfo() : TypeInfo() {
    abstract val newValue: String
    abstract fun newImmutable(from: String): String
}

class RepeatedTypeInfo(val element: SimpleTypeInfo) : CollectionTypeInfo() {
    override fun isJavaPrimitive(): Boolean = false
    override val javaName: String
        get() = when (element.javaName) {
            "double" -> "$protobuf.DoubleList"
            "float" -> "$protobuf.FloatList"
            "long" -> "$protobuf.LongList"
            "int" -> "$protobuf.IntList"
            "boolean" -> "$protobuf.BooleanList"
            else -> "java.util.List<${element.javaName}>"
        }
    override val defaultValue: String
        get() = when (element.javaName) {
            "double" -> "$protobuf.ImmutableDoubleList.EMPTY"
            "float" -> "$protobuf.ImmutableFloatList.EMPTY"
            "long" -> "$protobuf.ImmutableLongList.EMPTY"
            "int" -> "$protobuf.ImmutableIntList.EMPTY"
            "boolean" -> "$protobuf.ImmutableBooleanList.EMPTY"
            else -> "java.util.Collections.emptyList()"
        }
    override val newValue: String
        get() = when (element.javaName) {
            "double" -> "new $protobuf.DoubleList()"
            "float" -> "new $protobuf.FloatList()"
            "long" -> "new $protobuf.LongList()"
            "int" -> "new $protobuf.IntList()"
            "boolean" -> "new $protobuf.BooleanList()"
            else -> "new java.util.ArrayList<>()"
        }

    override fun newImmutable(from: String): String = when (element.javaName) {
        "double" -> "$protobuf.ImmutableDoubleList.wrap($from)"
        "float" -> "$protobuf.ImmutableFloatList.wrap($from)"
        "long" -> "$protobuf.ImmutableLongList.wrap($from)"
        "int" -> "$protobuf.ImmutableIntList.wrap($from)"
        "boolean" -> "$protobuf.ImmutableBooleanList.wrap($from)"
        else -> "$protobuf.Collections.makeImmutable($from)"
    }

    override fun toString(): String = "repeated $element"
}

class MapTypeInfo(
    val key: PrimitiveType,
    val value: SimpleTypeInfo,
) : CollectionTypeInfo() {
    override fun isJavaPrimitive(): Boolean = false
    override val javaName: String
        get() = "java.util.Map<${getWrappedType(key.javaName)}, ${getWrappedType(value.javaName)}>"
    override val defaultValue: String
        get() = "java.util.Collections.emptyMap()"
    override val newValue: String
        get() = "new java.util.HashMap<>()"

    override fun newImmutable(from: String): String = "$protobuf.Collections.makeImmutable($from)"

    override fun toString(): String = "map<$key, $value>"
}

sealed class SimpleTypeInfo : TypeInfo() {
    abstract val typeTag: TypeTag
    companion object {
        fun of(field: DescriptorProtos.FieldDescriptorProto, types: Map<FqName, UserTypeInfo>): SimpleTypeInfo {
            return when (field.type!!) {
                Type.TYPE_DOUBLE -> PrimitiveType.Double
                Type.TYPE_FLOAT -> PrimitiveType.Float

                Type.TYPE_INT64 -> PrimitiveType.Int64
                Type.TYPE_UINT64 -> PrimitiveType.Uint64
                Type.TYPE_FIXED64 -> PrimitiveType.Fixed64
                Type.TYPE_SFIXED64 -> PrimitiveType.SFixed64
                Type.TYPE_SINT64 -> PrimitiveType.SInt64

                Type.TYPE_INT32 -> PrimitiveType.Int32
                Type.TYPE_FIXED32 -> PrimitiveType.Fixed32
                Type.TYPE_SFIXED32 -> PrimitiveType.SFixed32
                Type.TYPE_UINT32 -> PrimitiveType.Uint32
                Type.TYPE_SINT32 -> PrimitiveType.SInt32

                Type.TYPE_BOOL -> PrimitiveType.Bool
                Type.TYPE_STRING -> PrimitiveType.String
                Type.TYPE_BYTES -> PrimitiveType.Bytes
                Type.TYPE_GROUP -> error("group is unsupported")

                Type.TYPE_MESSAGE -> types.getValue(FqName.from(field.typeName!!))
                Type.TYPE_ENUM -> types.getValue(FqName.from(field.typeName!!))
            }
        }
    }
}

@Suppress("SpellCheckingInspection")
sealed class PrimitiveType(val name: kotlin.String, override val javaName: kotlin.String) : SimpleTypeInfo() {
    override val defaultValue: kotlin.String
        get() = when (javaName) {
            "double" -> "0.0"
            "float" -> "0.0f"
            "long" -> "0L"
            "int" -> "0"
            "boolean" -> "false"
            "java.lang.String" -> "\"\""
            "com.anatawa12.protobuf.Bytes" -> "com.anatawa12.protobuf.Bytes.defaultValue"
            else -> "null"
        }
    override fun isJavaPrimitive(): Boolean = when (javaName) {
        "double" -> true
        "float" -> true
        "long" -> true
        "int" -> true
        "boolean" -> true
        else -> false
    }

    override fun toString(): kotlin.String = name

    object Double : PrimitiveType("double", "double") {
        override val typeTag get() = TypeTag.TYPE_64BIT
    }
    object Float : PrimitiveType("float", "float") {
        override val typeTag get() = TypeTag.TYPE_32BIT
    }
    object Int64 : PrimitiveType("int64", "long") {
        override val typeTag get() = TypeTag.TYPE_VARINT
    }
    object Uint64 : PrimitiveType("uint64", "long") {
        override val typeTag get() = TypeTag.TYPE_VARINT
    }
    object Fixed64 : PrimitiveType("fixed64", "long") {
        override val typeTag get() = TypeTag.TYPE_64BIT
    }
    object SFixed64 : PrimitiveType("sfixed64", "long") {
        override val typeTag get() = TypeTag.TYPE_64BIT
    }
    object SInt64 : PrimitiveType("sint64", "long") {
        override val typeTag get() = TypeTag.TYPE_VARINT
    }
    object Int32 : PrimitiveType("int32", "int") {
        override val typeTag get() = TypeTag.TYPE_VARINT
    }
    object Fixed32 : PrimitiveType("fixed32", "int") {
        override val typeTag get() = TypeTag.TYPE_32BIT
    }
    object SFixed32 : PrimitiveType("sfixed32", "int") {
        override val typeTag get() = TypeTag.TYPE_32BIT
    }
    object Uint32 : PrimitiveType("uint32", "int") {
        override val typeTag get() = TypeTag.TYPE_VARINT
    }
    object SInt32 : PrimitiveType("sint32", "int") {
        override val typeTag get() = TypeTag.TYPE_VARINT
    }
    object Bool : PrimitiveType("bool", "boolean") {
        override val typeTag get() = TypeTag.TYPE_VARINT
    }
    object String : PrimitiveType("string", "java.lang.String") {
        override val typeTag get() = TypeTag.TYPE_DELIMITED
    }
    object Bytes : PrimitiveType("bytes", "com.anatawa12.protobuf.Bytes") {
        override val typeTag get() = TypeTag.TYPE_DELIMITED
    }
}

sealed class UserTypeInfo : SimpleTypeInfo() {
    override fun isJavaPrimitive(): Boolean = false
    abstract val fqName: FqName

    protected abstract val javaPackage: String?
    override val javaName: String
        get() = if (javaPackage == null) fqName.javaName else "$javaPackage.${fqName.simpleName}"
    override val defaultValue: String get() = "$javaName.defaultValue"

    override fun toString(): String = fqName.toString()

    open fun createMessageInfo(types: Map<FqName, UserTypeInfo>) {
    }
}

class MessageInfo(
    override val fqName: FqName,
    override val javaPackage: String?,
    val real: DescriptorProtos.DescriptorProto,
) : UserTypeInfo() {
    override val typeTag get() = TypeTag.TYPE_DELIMITED
    lateinit var nestedTypes: Map<String, UserTypeInfo>
    lateinit var fields: List<FieldInfo>
    lateinit var oneofNameList: List<String>

    override fun createMessageInfo(types: Map<FqName, UserTypeInfo>) {
        val fields = mutableListOf<FieldInfo>()

        // generate fields
        for (field in real.fieldList) {
            val typeName = TypeInfo.of(field, types)
            if (field.hasOneofIndex()) {
                fields.add(FieldInfo(field.name,
                    field.number,
                    typeName,
                    true,
                    field.oneofIndex))
            } else {
                fields.add(FieldInfo(field.name,
                    field.number,
                    typeName,
                    false,
                    0))
            }
        }

        this.fields = fields
        this.oneofNameList = real.oneofDeclList.map { it.name }
    }
}

class FieldInfo(
    val name: String,
    val number: Int,
    val type: TypeInfo,
    val oneOf: Boolean,
    val index: Int,
)

class EnumInfo(
    override val fqName: FqName,
    override val javaPackage: String?,
    val real: DescriptorProtos.EnumDescriptorProto,
) : UserTypeInfo() {
    override val typeTag get() = TypeTag.TYPE_VARINT
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
inline class FqName private constructor(
    /**
     * starts with '.', not ends with '.'
     */
    val name: String,
) {
    fun resolve(name: String) = FqName("${this.name}.$name")

    val packageName get() = name.substringBeforeLast('.').drop(1)
    val simpleName get() = name.substringAfterLast('.')
    val javaName get() = name.drop(1)

    override fun toString(): String = name

    companion object {
        fun from(name: String): FqName {
            if (name.isEmpty()) return FqName("")
            if (name == ".") return FqName(".")
            // remove last '.'
            val name1 = if (name.last() != '.') name else name.dropLast(1)
            return if (name.first() == '.') FqName(name1) else FqName(".$name")
        }
    }
}

private object Types {
    fun getWrappedType(javaName: String): String = when (javaName) {
        "double" -> "Double"
        "float" -> "Float"
        "long" -> "Long"
        "int" -> "Integer"
        "boolean" -> "Boolean"
        else -> javaName
    }
}
