import uniffi.poke_engine_ffi.testConnection

fun main() {
    System.setProperty(
        "jna.library.path",
        "/Users/jayden/Documents/MegaGliscor/poke-engine-ffi/target/release"
    )
    System.load(
        "/Users/jayden/Documents/MegaGliscor/poke-engine-ffi/target/release/libpoke_engine_ffi.dylib"
    )

    println("Testing Rust connection...")

    val result = testConnection()

    println(result)
}