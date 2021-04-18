package com.anatawa12.protobuf.compiler

inline fun buildSource(block: SourceBuilder.() -> Unit): Source {
    return SourceBuilder().apply(block).build()
}

inline fun buildSource(begin: String, block: SourceBuilder.() -> Unit): Source {
    return SourceBuilder().apply { block(begin, block) }.build()
}

class SourceBuilder {
    private val lines = mutableListOf<String>()

    operator fun String.unaryPlus() = appendln(this)
    operator fun Source.unaryPlus() = appendln(this)

    fun appendln(line: String) {
        lines += line
    }

    fun appendln(region: Source) {
        lines += region.lines
    }

    fun indent() {
        lines += "\$>>\$"
    }

    fun dedent() {
        lines += "\$<<\$"
    }

    inline fun block(begin: String, block: SourceBuilder.() -> Unit) {
        appendln("$begin {")
        indent()
        block(this)
        dedent()
        appendln("}")
    }

    fun build() = Source(lines.toList())
}

class Source(val lines: List<String>) {
    override fun toString(): String = toString("  ", "\n")
    fun toString(indent: String, lineSeparator: String): String = buildString {
        val indents = mutableListOf<String>()
        for (line in lines) {
            if (line == "\$>>\$") {
                indents.add(indent)
            } else if (line == "\$<<\$") {
                indents.removeLast()
            } else if (line == "") {
                append(lineSeparator)
            } else {
                for (append in indents) append(append)
                append(line).append(lineSeparator)
            }
        }
    }
}
