package states.pokemonstates

data class StatRange(
    val ranges: Map<Stat, IntRange>
) {
    fun get(stat: Stat): IntRange {
        return ranges[stat] ?: (0..0)
    }
}