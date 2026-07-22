package states

import states.pokemonstates.AbilityState
import states.pokemonstates.ItemState
import states.pokemonstates.MoveState
import states.pokemonstates.Species
import states.pokemonstates.StatState
import states.pokemonstates.StatusState
import states.pokemonstates.VolatileStatusState
import kotlin.reflect.KClass

data class PokemonState(
    val species: Species,

    var hp: Int,

    val maxHp: Int,

    var stats: StatState,

    var status: StatusState,

    val moves: MutableList<MoveState>,

    var item: ItemState,

    var ability: AbilityState,

    private val volatileStatuses: MutableList<VolatileStatusState> = mutableListOf()
) {

    fun hasVolatile(statusClass: KClass<out VolatileStatusState>): Boolean {
        return volatileStatuses.any {
            it::class == statusClass
        }
    }

    fun addVolatile(status: VolatileStatusState) {
        if (!hasVolatile(status::class)) {
            volatileStatuses.add(status)
        }
    }

    fun removeVolatile(statusClass: KClass<out VolatileStatusState>) {
        volatileStatuses.removeIf {
            it::class == statusClass
        }
    }

    fun getVolatileStatuses(): List<VolatileStatusState> {
        return volatileStatuses
    }
}