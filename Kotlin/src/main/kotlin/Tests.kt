import LoggerConfigs.rustBindingLogger
import uniffi.poke_engine_ffi.testConnection

object Tests {

    fun connectionTest() {
        rustBindingLogger.i {"Connection test"}

        val result = testConnection()

        println(result)
    }
}