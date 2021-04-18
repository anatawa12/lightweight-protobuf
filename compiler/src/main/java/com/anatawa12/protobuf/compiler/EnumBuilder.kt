package com.anatawa12.protobuf.compiler

object EnumBuilder {
    fun generateEnum(enum: EnumInfo): Source {
        val simpleName = enum.fqName.simpleName

        val constants = mutableListOf<String>()
        val fields = mutableListOf<String>()

        val generated = mutableMapOf<Int, String>()
        for (enumValue in enum.real.valueList) {
            if (enumValue.number in generated) {
                fields += "public static final ${enum.javaName} ${enumValue.name} = ${generated[enumValue.number]};"
            } else {
                generated[enumValue.number] = enumValue.name
                constants += "${enumValue.name}(${enumValue.number}),"
            }
        }

        return buildSource("public enum $simpleName") {
            for (constant in constants) +constant
            +";"
            for (field in fields) +field
            +"public static final ${enum.javaName} defaultValue = ${enum.real.valueList[0].name};"
            +""
            +"private final int \$id;"
            +""
            block("$simpleName(int id)") {
                +"this.\$id = id;"
            }
            +""
            block("public int getId()") {
                +"return this.\$id;"
            }
            +""
            block("public static ${enum.javaName} fromId(int id)") {
                block("switch (id)") {
                    for ((id, name) in generated) {
                        +"case $id: return $name;"
                    }
                    +"default: return null;"
                }
            }
        }
    }

}
