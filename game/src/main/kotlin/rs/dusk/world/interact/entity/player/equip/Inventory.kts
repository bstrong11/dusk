package rs.dusk.world.interact.entity.player.equip

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.entity.character.contain.inventory
import rs.dusk.engine.entity.character.contain.sendContainer
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.display.InterfaceInteraction
import rs.dusk.world.interact.entity.player.display.InterfaceSwitch

val logger = InlineLogger()
val decoder: ItemDecoder by inject()
val bus: EventBus by inject()

InterfaceOpened where { name == "inventory" } then {
    player.interfaces.sendSetting(name, "container", 0, 27, 4554126)// Item slots
    player.interfaces.sendSetting(name, "container", 28, 55, 2097152)// Draggable slots
    player.sendContainer(name)
}

InterfaceSwitch where { name == "inventory" && toName == "inventory" } then {
    val container = player.inventory
    var fromItemId = fromItemId

    if (fromItemId == -1) {
        if (!container.inBounds(fromSlot)) {
            logger.debug { "Interface $toId component $toComponentId from slot $fromSlot not found for player $player" }
            return@then
        }

        fromItemId = container.getItem(fromSlot)
    }

    if (!container.isValidId(fromSlot, fromItemId)) {
        logger.debug { "Interface $id component $componentId from item $fromItemId slot $fromSlot not found for player $player" }
        return@then
    }

    val toSlot = toSlot - 28
    if (!container.isValidId(toSlot, toItemId)) {
        logger.debug { "Interface $toId component $toComponentId to item $toItemId slot $toSlot not found for player $player" }
        return@then
    }
    player.inventory.switch(fromSlot, toSlot)
}

InterfaceInteraction where { name == "inventory" && component == "container" } then {
    val itemDef = decoder.getSafe(itemId)
    val equipOption = when (optionId) {
        7 -> if (itemDef.options.any { it == "Destroy" }) "Destroy" else "Drop"
        8 -> "Examine"
        else -> itemDef.options.getOrNull(optionId - 1)
    }
    if (equipOption == null) {
        logger.info { "Unknown item option $itemId $optionId" }
        return@then
    }
    bus.emit(
        ContainerAction(
            player,
            name,
            item,
            itemIndex,
            equipOption
        )
    )
}