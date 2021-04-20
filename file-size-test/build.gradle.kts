import java.io.PrintWriter

plugins {
    id("com.github.johnrengelman.shadow")
    java
}

project("wire") {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "java")

    repositories.mavenCentral()
    dependencies {
        implementation("com.squareup.wire:wire-runtime:${Versions.wire}")
    }
}

project("wire-no-kt") {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "java")

    repositories.mavenCentral()
    dependencies {
        implementation("com.squareup.wire:wire-runtime:${Versions.wire}") {
            exclude(group = "org.jetbrains.kotlin")
            exclude(group = "org.jetbrains")
        }
    }
}

project("google") {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "java")

    repositories.mavenCentral()
    dependencies {
        implementation("com.google.protobuf:protobuf-java:${Versions.protobuf}")
    }
}

project("google-lite") {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "java")

    repositories.mavenCentral()
    dependencies {
        implementation("com.google.protobuf:protobuf-javalite:${Versions.protobuf}")
    }
}

project("lightweight") {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "java")

    repositories.mavenCentral()
    dependencies {
        implementation(project(":"))
    }
}

fun String.threeCharSep(): String {
    val thisSize = this.length
    val sb = StringBuilder(thisSize + thisSize / 3)
    var index = thisSize % 3
    sb.append(this, 0, index)
    while (index in 0 until thisSize) {
        val end = index + 3
        sb.append(' ')
        sb.append(this, index, end.coerceAtMost(thisSize))
        index = end
    }
    return sb.toString()
}

val generateFatSizes by tasks.creating {
    dependsOn(subprojects.map { it.tasks.getByName("shadowJar") })
    inputs.files(subprojects.map { it.tasks.getByName<Jar>("shadowJar").archiveFile })
    doLast {
        file("fat-sizes.txt").bufferedWriter().let { PrintWriter(it) }.use {
            it.println("common:")
            it.println("  wire: ${Versions.wire}")
            it.println("  protobuf: ${Versions.protobuf}")
            it.println("  lightweight: ${project(":").version}")
            it.println("  git-hash: ${getGitHash()}")
            subprojects.forEach { project ->
                val length = project.tasks.getByName<Jar>("shadowJar").archiveFile.get().asFile.length()
                it.println("${project.name}:".padEnd(12, ' ') +
                        length.toString().padStart(8, ' ').threeCharSep() +
                        " bytes")
            }
        }
    }
}

fun getGitHash(): String {
    return kotlin.runCatching {
        ProcessBuilder().command("git", "rev-parse", "--short", "HEAD")
            .start().inputStream.bufferedReader().use { it.readText() }
            .lineSequence().firstOrNull()
    }.getOrNull() ?: "unknown"
}
