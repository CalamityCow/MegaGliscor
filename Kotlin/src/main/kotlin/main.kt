import LoggerConfigs.generalLogger
import PSInterface.connectAndLogin
import PSInterface.setAvatar
import WebsocketClient.close
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {

    Runtime.getRuntime().addShutdownHook(
        Thread {
            generalLogger.i("Application shutting down.")

            runBlocking {
                close()
            }
        }
    )

    System.setProperty(
        "jna.library.path",
        "/Users/jayden/Documents/MegaGliscor/poke-engine-ffi/target/release"
    )

    System.load(
        "/Users/jayden/Documents/MegaGliscor/poke-engine-ffi/target/release/libpoke_engine_ffi.dylib"
    )

    val engineBot = BotPlayer(
        Config.username,
        Config.password,
        Config.avatar,
        Config.address
    )

    connectAndLogin(engineBot)
    setAvatar(engineBot)


    awaitCancellation()
}