package states.pokemonstates

data class StatusState(
    var status: Status,
    var turns: Int?
)

enum class Status {
    NONE,
    BURN,
    POISON,
    TOXIC,
    PARALYSIS,
    SLEEP,
    REST,
    FREEZE
}
