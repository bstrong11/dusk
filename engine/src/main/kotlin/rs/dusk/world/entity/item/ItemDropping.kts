import kotlinx.coroutines.cancel
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.action.Scheduler
import rs.dusk.engine.action.delay
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.Registered
import rs.dusk.engine.model.entity.item.FloorItem
import rs.dusk.engine.model.entity.item.FloorItemState
import rs.dusk.engine.model.entity.item.FloorItems
import rs.dusk.engine.model.entity.item.offset
import rs.dusk.engine.model.world.Tile
import rs.dusk.network.rs.codec.game.encode.message.FloorItemAddMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRemoveMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemRevealMessage
import rs.dusk.network.rs.codec.game.encode.message.FloorItemUpdateMessage
import rs.dusk.utility.inject
import rs.dusk.world.entity.item.Drop

val decoder: ItemDecoder by inject()
val items: FloorItems by inject()
val scheduler: Scheduler by inject()
val bus: EventBus by inject()

Drop then {
    val definition = decoder.getSafe(id)
    if(definition.stackable == 1) {
        val existing = items.getExistingStack(tile, id)
        if(existing != null && combinedStacks(existing, amount, disappearTicks)) {
            return@then
        }
    }
    val item = FloorItem(tile, id, amount)
    items.add(item)
    items.update(tile, FloorItemAddMessage(item.tile.offset(), item.id, item.amount))
    reveal(item, revealTicks, owner)
    disappear(item, disappearTicks)
    bus.emit(Registered(item))
}


fun FloorItems.getExistingStack(tile: Tile, id: Int): FloorItem? {
    return get(tile)?.firstOrNull { it.tile == tile && it.state == FloorItemState.Private && it.id == id }
}

/**
 * Combines stacks of two items and resets the disappear count down
 * Note: If total of combined stacks exceeds [Int.MAX_VALUE] then returns false
 */
fun combinedStacks(existing: FloorItem, amount: Int, disappearTicks: Int): Boolean {
    val stack = existing.amount
    val combined = stack + amount
    // Overflow should add as separate item
    if (stack xor combined and (amount xor combined) < 0) {
        return false
    }
    existing.amount = combined
    items.update(existing.tile, FloorItemUpdateMessage(existing.tile.offset(), existing.id, stack, combined))
    existing.disappear?.cancel("Floor item disappear time extended.")
    disappear(existing, disappearTicks)
    return true
}

/**
 * Schedules disappearance after [ticks]
 */
fun disappear(item: FloorItem, ticks: Int) {
    if(ticks >= 0) {
        item.disappear = scheduler.add {
            delay(ticks)
            item.state = FloorItemState.Removed
            items.update(item.tile, FloorItemRemoveMessage(item.tile.offset(), item.id))
            items.remove(item)
        }
    }
}

/**
 * Schedules public reveal of [owner]'s item after [ticks]
 */
fun reveal(item: FloorItem, ticks: Int, owner: Int) {
    if(ticks >= 0) {
        scheduler.add {
            delay(ticks)
            item.state = FloorItemState.Public
            items.update(item.tile, FloorItemRevealMessage(item.tile.offset(), item.id, item.amount, owner))
        }
    }
}