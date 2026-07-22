package states.pokemonstates

data class StatState(
    val knownBuild: StatBuild?,
    val estimatedStats: StatRange?,
    val statBoosts: StatBoosts
)