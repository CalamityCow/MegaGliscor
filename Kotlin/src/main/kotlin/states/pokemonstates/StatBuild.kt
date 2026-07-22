package states.pokemonstates

enum class Stat {
    HP,
    ATTACK,
    DEFENSE,
    SP_ATTACK,
    SP_DEFENSE,
    SPEED
}

data class StatBuild(
    val evs: EVs,
    val ivs: IVs,
    val nature: Nature
)

data class EVs(val values: Map<Stat, Int>) {
    init {
        require(values.values.all { it in 0..252 }) {
            "Individual EV values must be between 0 and 252"
        }

        require(values.values.sum() <= 510) {
            "Total EVs cannot exceed 510"
        }
    }

    fun get(stat: Stat): Int {
        return values[stat] ?: 0
    }
}

data class IVs(
    val values: Map<Stat, Int>
) {
    init {
        require(values.values.all { it in 0..31 }) {
            "IV values must be between 0 and 31"
        }
    }

    fun get(stat: Stat): Int {
        return values[stat] ?: 31
    }
}

data class Nature(
    val increased: Stat?,
    val decreased: Stat?
) {
    fun multiplier(stat: Stat): Double {
        return when (stat) {
            increased -> 1.1
            decreased -> 0.9
            else -> 1.0
        }
    }
}