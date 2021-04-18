plugins {
    kotlin("jvm") version "1.4.31"
    id("com.google.protobuf") version "0.8.15" apply false
    java
    idea
    application
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
    from (fileTree("compiler/src/main/proto"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}
