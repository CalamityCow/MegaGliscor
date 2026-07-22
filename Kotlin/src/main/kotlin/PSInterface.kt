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
        WebsocketClient.setAddress(player.address)
        WebsocketClient.connect()
        val challstr = returnChallstr()
        val assertion = getAssertion(player, challstr)
        WebsocketClient.sendMessage("|/trn ${player.user},0,$assertion")
        LoggerConfigs.websocketLogger.i {"Successfully logged in as ${player.user}"}
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
        val response = WebsocketClient.httpPost(
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
        WebsocketClient.sendMessage("|/avatar ${player.avatar}")
    }

    /**
     * Searches for a battle in a given format.
     *
     * @param format The battle format e.g. gen9ou, ranbats
     */
    suspend fun searchBattle(format: String) {
        WebsocketClient.sendMessage("|/search $format")
    }

    /**
     * Sends a challenge to a user in a specific format.
     *
     * @param username The username of the user to challenge.
     * @param format The battle format e.g. gen9ou, ranbats
     */
    suspend fun sendChallenge(username: String, format: String) {
        WebsocketClient.sendMessage("|/challenge $username, $format")
    }

    /**
     * Accepts incoming challenge from a player
     *
     * @param username The username of the challenger
     */
    suspend fun acceptChallenge(username: String) {
        WebsocketClient.sendMessage("|/accept $username")
    }
}