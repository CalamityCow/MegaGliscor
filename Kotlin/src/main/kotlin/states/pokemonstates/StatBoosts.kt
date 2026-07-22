package states.pokemonstates

data class StatBoosts(
    var boosts: MutableMap<Stat, Int> = mutableMapOf()
) {

    fun get(stat: Stat): Int {
        return boosts[stat] ?: 0
    }

    fun set(stat: Stat, value: Int) {
        boosts[stat] = value.coerceIn(-6, 6)
    }

    fun increase(stat: Stat, amount: Int) {
        set(stat, get(stat) + amount)
    }
}