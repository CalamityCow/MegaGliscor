package states.pokemonstates

import states.PokemonState

sealed class VolatileStatusState {

    data class Confusion(
        var turns: Int
    ) : VolatileStatusState()

    data class Substitute(
        var hp: Int
    ) : VolatileStatusState()

    data class Taunt(
        var turns: Int
    ) : VolatileStatusState()

    data class Encore(
        var turns: Int,
        val move: Move
    ) : VolatileStatusState()

    data class LeechSeed(
        val source: PokemonState
    ) : VolatileStatusState()

    data class Protect(
        var consecutiveUses: Int
    ) : VolatileStatusState()
}