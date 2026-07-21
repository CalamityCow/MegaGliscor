import co.touchlab.kermit.Logger

object LoggerConfigs {
    val rustBindingLogger = Logger.withTag("rust-binding-logger")
    val websocketLogger = Logger.withTag("websocket-logger")
    val generalLogger = Logger.withTag("general-logger")
}