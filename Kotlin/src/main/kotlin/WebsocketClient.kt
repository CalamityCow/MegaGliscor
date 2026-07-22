import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.awt.SystemColor

object WebsocketClient {
    private var address = ""
    private lateinit var session: DefaultClientWebSocketSession
    private val _messages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 100
    )

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val messages: SharedFlow<String> = _messages
    private val client = HttpClient(CIO) {
        HttpClientConfig.install(WebSockets.Plugin)
    }

    fun setAddress(newAddress: String) {
        address = newAddress
    }

    fun getAddress(): String {
        return address
    }

    suspend fun connect() {
        session = client.webSocketSession(address)
        scope.launch {
                while (true) {
                    val frame = session.incoming.receive()
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        LoggerConfigs.websocketLogger.i { "Received: $text" }
                        _messages.emit(text)
                    }
                    else { LoggerConfigs.websocketLogger.w { "Non text received: ${SystemColor.text}" } }
                }
            }
    }

    suspend fun sendMessage(text: String) {
        session.send(Frame.Text(text))
        LoggerConfigs.websocketLogger.i { "Message sent $text" }
    }

    suspend fun httpPost(body: Parameters, url: String): String {
        val response = client.post(url) {
            setBody(FormDataContent(body))
        }
        return response.bodyAsText()
    }

    suspend fun waitForMessage(predicate: (String) -> Boolean): String {
        return messages.first { message -> predicate(message) }

    }

    suspend fun close() {
        session.close()
        client.close()
    }
}