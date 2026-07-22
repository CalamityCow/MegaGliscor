package states.pokemonstates

sealed interface ItemState

data class KnownItem(val item: Item) : ItemState
data object UnknownItem : ItemState
data object NoItem : ItemState

enum class Item {
}