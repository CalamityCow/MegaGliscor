package states.pokemonstates

sealed interface AbilityState

data class KnownAbility(val ability: Abilities) : AbilityState
data object UnknownAbility : AbilityState

enum class Abilities {
}