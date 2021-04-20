import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.google.protobuf")
    java
    application
    idea
}

group = project(":").group
version = project(":").version

repositories {
    mavenCentral()
}

idea.module.sourceDirs.add(file("../benchmark/src/main/proto-include"))

dependencies {
    implementation("com.google.protobuf:protobuf-java:${Versions.protobuf}")
    implementation(kotlin("stdlib"))
    testImplementation(project(":"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:1.7.0")
}

application {
    // use mainClassName for shadow plugin.
    // see https://github.com/johnrengelman/shadow/issues/609
    // see https://github.com/johnrengelman/shadow/pull/612

    //mainClass.set("NativeWrapperRunner")
    @Suppress("DEPRECATION")
    mainClassName = "com.anatawa12.protobuf.compiler.PluginMain"
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${Versions.protobuf}"
    }
    plugins {
        create("lw-java") {
            //artifact = "com.anatawa12.lightweight-protobuf:compiler:$version"
            path = tasks.shadowJar.get().archiveFile.get().asFile.path
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            // workaround for https://github.com/google/protobuf-gradle-plugin/issues/470
            it.addIncludeDir(files("../benchmark/src/main/proto-include/"))
        }
        ofSourceSet("test").forEach {
            // workaround for https://github.com/google/protobuf-gradle-plugin/issues/470
            it.addIncludeDir(files("../benchmark/src/main/proto-include/"))
            it.addIncludeDir(files("src/main/proto/"))
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
