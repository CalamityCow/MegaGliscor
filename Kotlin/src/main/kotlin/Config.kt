import io.github.cdimascio.dotenv.dotenv

object Config {
    private val env = dotenv {
        directory = "/Users/jayden/Documents/MegaGliscor"
        filename = "dev.env"
    }

    val username = env["PS_USERNAME"] ?: error("USERNAME not found in environment variables")
    val password = env["PS_PASSWORD"] ?: error("PASSWORD not found in env variables")
    val avatar = env["PS_AVATAR"] ?: error("AVATAR not found in env variables")
    val address = env["PS_ADDRESS"] ?: error("ADDRESS not found in env variables")
}