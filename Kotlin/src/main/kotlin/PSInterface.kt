import LoggerConfigs.websocketLogger
import WebsocketClient.connect
import WebsocketClient.httpPost
import WebsocketClient.sendMessage
import WebsocketClient.setAddress
import co.touchlab.kermit.Logger
import io.ktor.http.Parameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object PSInterface {

    /**
     * Connects then logs in the player to the Pokémon Showdown server.
     *
     * @param player The bot player to log in.
     */
    suspend fun connectAndLogin(player: BotPlayer) {
        setAddress(player.address)
        connect()
        val challstr = returnChallstr()
        val assertion = getAssertion(player, challstr)
        sendMessage("|/trn ${player.user},0,$assertion")
        websocketLogger.i {"Successfully logged in as ${player.user}"}
    }

    /**
     * Upon connecting to PS server, we should receive a string in format:
     * |challstr|<str1>|<str2>
     *
     * @return The challstr in the format <str1>|<str2>
     */
    suspend fun returnChallstr(): String {
        val message = WebsocketClient.waitForMessage {
            msg -> val split = msg.split("|")
            split.size >= 4 && split[1] == "challstr"
        }
        val split = message.split("|")
        return "${split[2]}|${split[3]}"
    }
    /**
     * Retrieves the assertion for the given player and challstr.
     * Server will return a JSON after we http post in the format:
     * ]{"actionsuccess":true,"assertion":"<assertion string>"}
     *
     * @param player The bot player for which to retrieve the assertion.
     * @param challstr The challenge string received from the server.
     * @return The assertion string.
     */
    suspend fun getAssertion(player: BotPlayer, challstr: String): String {
        val response = httpPost(
            Parameters.build {
                append("name", player.user)
                append("pass", player.password)
                append("challstr", challstr)
            },
            "https://play.pokemonshowdown.com/api/login"
        )
        val json = response.removePrefix("]")

        val parsed = Json.parseToJsonElement(json).jsonObject

        val success = parsed["actionsuccess"]
            ?.jsonPrimitive
            ?.boolean ?: false

        if (!success) {
            error("Login failed: ${parsed["message"]}")
        }

        return parsed["assertion"]
            ?.jsonPrimitive
            ?.content
            ?: error("Assertion not found in response")
    }

    /**
     * Sets the avatar for the given player.
     *
     * @param player The bot player
     */
    suspend fun setAvatar(player: BotPlayer) {
        sendMessage("|/avatar ${player.avatar}")
    }

    
}