plugins {
    kotlin("jvm") version "2.3.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.java.dev.jna:jna:5.14.0")
}

application {
    mainClass.set("MainKt")

    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED"
    )
}

kotlin {
    jvmToolchain(24)

    sourceSets {
        main {
            kotlin.srcDir("../poke-engine-ffi/bindings-out")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Exec>("buildRust") {
    workingDir("../poke-engine-ffi")

    commandLine(
        "/Users/jayden/.cargo/bin/cargo",
        "build",
        "--release"
    )
}

tasks.register<Exec>("generateUniFFI") {
    dependsOn("buildRust")

    workingDir("../poke-engine-ffi")

    commandLine(
        "/Users/jayden/.cargo/bin/cargo",
        "run",
        "-p",
        "uniffi-bindgen",
        "--",
        "generate",
        "--library",
        "target/release/libpoke_engine_ffi.dylib",
        "--language",
        "kotlin",
        "--out-dir",
        "bindings-out"
    )
}

tasks.named("compileKotlin") {
    dependsOn("generateUniFFI")
}