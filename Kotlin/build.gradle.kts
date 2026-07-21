val jnaVersion = "5.14.0"
val ktorVersion = "3.5.1"
val kermitVersion = "2.0.4"
val kotLinxVersion = "1.11.0"
val dotENVKotlinVersion = "6.5.1"
val logBackClassicVersion = "1.5.38"

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.3.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.java.dev.jna:jna:$jnaVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("co.touchlab:kermit:$kermitVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotLinxVersion")
    implementation("io.github.cdimascio:dotenv-kotlin:$dotENVKotlinVersion")
    implementation("ch.qos.logback:logback-classic:$logBackClassicVersion")
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
    description = "Build the Rust library using Cargo"
    group = "build"
    workingDir("../poke-engine-ffi")

    commandLine(
        "/Users/jayden/.cargo/bin/cargo",
        "build",
        "--release"
    )
}

tasks.register<Exec>("generateUniFFI") {
    description = "Generate UniFFI"
    group = "build"
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

tasks.register<Exec>("launchBot") {
    description = "Launch the bot"
    group = "launch"
    dependsOn("installDist")

    commandLine(
        "bash",
        "${project.buildDir}/install/${project.name}/bin/${project.name}"
    )
}