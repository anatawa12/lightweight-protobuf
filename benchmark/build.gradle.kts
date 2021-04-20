import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm")
    id("me.champeau.jmh")
    id("com.google.protobuf")
    application
    id("com.squareup.wire") version Versions.wire
    java
    idea
}

group = project(":").group
version = project(":").version

configurations.jmhImplementation.get().resolutionStrategy

repositories {
    mavenCentral()
}

idea.module.sourceDirs.add(file(relativePath("src/main/proto-include")))

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.google.protobuf:protobuf-java:${Versions.protobuf}")
    implementation(project(":"))
    implementation(project(":compiler"))
    implementation("com.squareup.wire:wire-runtime:${Versions.wire}")
}

wire {
    java {}
    protoPath {
        srcProject(":")
    }
}

jmh {
    jmhVersion.set("1.29")
    warmupIterations.set(5)
    iterations.set(5)
    fork.set(2)
    benchmarkMode.add("thrpt")
    failOnError.set(true)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.protobuf}"
    }
    plugins {
        create("lw-java") {
            //artifact = "com.anatawa12.lightweight-protobuf:compiler:$version:all@jar"
            path = tasks.getByPath(":compiler:shadowJar").let { it as Jar }.archiveFile.get().asFile.path
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            // workaround for https://github.com/google/protobuf-gradle-plugin/issues/470
            it.addIncludeDir(files("src/main/proto-include/"))
            it.dependsOn(":compiler:shadowJar")
            it.plugins {
                create("lw-java")
            }
            it.outputs.upToDateWhen { false }
        }
    }
}

afterEvaluate {
    // workaround for https://github.com/google/protobuf-gradle-plugin/issues/470
    tasks.withType(ProtobufExtract::class) {
        isEnabled = false
    }
}
