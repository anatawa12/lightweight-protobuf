package com.anatawa12.protobuf.compiler

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse

object PluginMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val extensions = ExtensionRegistry.newInstance()
        Options.registerAllExtensions(extensions)
        val request = CodeGeneratorRequest.parseFrom(System.`in`, extensions)
        val systemOut = System.out
        System.setOut(System.err)

        val protoMap: Map<String, ProtobufFile> = kotlin.run {
            val map = mutableMapOf<String, ProtobufFile>()
            for (file in request.protoFileList) {
                map[file.name] = collectTypes(file)
            }
            map
        }

        checkDuplicate(protoMap)?.let { error ->
            CodeGeneratorResponse.newBuilder()
                .setError(error)
                .build()
                .writeTo(systemOut)
            return
        }
        val types = protoMap.values
            .fold(mutableMapOf<FqName, UserTypeInfo>()) { map, file -> map.apply { putAll(file.types) } }

        for (value in types.values) {
            value.createMessageInfo(types)
        }

        val fileList = mutableListOf<CodeGeneratorResponse.File>()
        for (s in request.fileToGenerateList) {
            generate(protoMap[s]!!, fileList)
        }

        CodeGeneratorResponse.newBuilder()
            .addAllFile(fileList)
            .build()
            .writeTo(systemOut)
    }

    private fun checkDuplicate(protoMap: Map<String, ProtobufFile>): String? {
        val typeDeclFileByFqName = mutableMapOf<FqName, MutableList<ProtobufFile>>()
        for (value in protoMap.values)
            for (key in value.types.keys)
                typeDeclFileByFqName.getOrPut(key, ::mutableListOf).add(value)

        val it = typeDeclFileByFqName.iterator()
        while (it.hasNext())
            if (it.next().value.size == 1)
                it.remove()

        if (typeDeclFileByFqName.isEmpty()) return null

        val error = StringBuilder()
        for ((fqName, typeDeclFiles) in typeDeclFileByFqName) {
            error.appendLine("$fqName duplicated in some files")
            for (typeDeclFile in typeDeclFiles)
                error.appendLine("  ${typeDeclFile.real.name}")
        }
        return error.toString()
    }

    private fun generate(proto: ProtobufFile, fileList: MutableList<CodeGeneratorResponse.File>) {
        for (rootType in proto.rootTypes) {
            val packageName = rootType.javaName.substringBeforeLast('.', "")
            val simpleName = rootType.javaName.substringAfterLast('.')
            val source = when (rootType) {
                is EnumInfo -> EnumBuilder.generateEnum(rootType)
                is MessageInfo -> MessageBuilder.generateMessage(rootType)
            }
            fileList += generateSourceFile(packageName, simpleName, source)
        }
    }

    private fun generateSourceFile(packageName: String, name: String, body: Source): CodeGeneratorResponse.File {
        val source = buildSource {
            +"// GENERATED BY LIGHTWEIGHT PROTOBUF BY ANATAWA12"
            +"// SEE https://github.com/anatawa12/lightweight-protobuf"
            +"// DO NOT EDIT THIS"
            if (packageName != "") {
                +""
                +"package $packageName;"
            }
            +""
            +body
        }
        return CodeGeneratorResponse.File.newBuilder()
            .setName(packageName.replace('.', '/') + "/" + name + ".java")
            .setContent(source.toString("  ", System.lineSeparator()))
            .build()
    }

    private fun stringList() = mutableListOf<String>()
    private fun sourceList() = mutableListOf<Source>()
}
