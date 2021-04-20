plugins {
    kotlin("jvm") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "6.1.0" apply false
    id("com.google.protobuf") version "0.8.15" apply false
    id("me.champeau.jmh") version "0.6.4" apply false
    java
    idea
    application
    signing
    `maven-publish`
}

group = "com.anatawa12.lightweight-protobuf"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:1.7.0")
}

tasks.processResources {
    from(fileTree("compiler/src/main/proto"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

listOf(
    project(":"),
    project(":compiler"),
).each {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "java")

    val maven = publishing.publications.create("maven", MavenPublication::class) {
        from(project.components["java"])

        pom {
            name.set(base.archivesBaseName)
            description.set("A lightweight protocol buffer implementation for java.")
            url.set("https://github.com/anatawa12/lightweight-protobuf")

            scm {
                url.set("https://github.com/anatawa12/lightweight-protobuf")
                connection.set("scm:git:git://github.com/anatawa12/lightweight-protobuf.git")
                developerConnection.set("scm:git:git@github.com:anatawa12/lightweight-protobuf.git")
            }

            issueManagement {
                system.set("github")
                url.set("https://github.com/anatawa12/lightweight-protobuf/issues")
            }

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("anatawa12")
                    name.set("anatawa12")
                    roles.set(setOf("developer"))
                }
            }
        }
    }

    publishing.repositories {
        maven {
            name = "mavenCentral"
            url = if (version.toString().endsWith("SNAPSHOT"))
                uri("https://oss.sonatype.org/content/repositories/snapshots")
            else uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = project.findProperty("com.anatawa12.sonatype.username")?.toString() ?: ""
                password = project.findProperty("com.anatawa12.sonatype.passeord")?.toString() ?: ""
            }
        }
    }

    signing.sign(maven)
}

fun <E> Iterable<E>.each(function: Action<E>) = forEach { function.execute(it!!) }
