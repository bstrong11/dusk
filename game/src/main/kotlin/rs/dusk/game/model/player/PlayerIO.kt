package rs.dusk.game.model.player

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.InterfaceManager
import rs.dusk.engine.client.ui.InterfaceOptions
import rs.dusk.engine.client.ui.PlayerInterfaceIO
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.definition.ContainerDefinitions
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.io.file.FileIO
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.path.strat.FollowTargetStrategy
import rs.dusk.engine.path.strat.RectangleTargetStrategy
import rs.dusk.network.rs.codec.game.encode.message.SkillLevelMessage
import rs.dusk.utility.get
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @author Greg Hibberd <greg@greghibberd.com>
 *
 * @since April 03, 2020
 */
class PlayerIO {

    private val logger = InlineLogger()

    private val path = "${getProperty<String>("local_files_path")}/players/"

    private val x = getProperty("homeX", 0)
    private val y = getProperty("homeY", 0)
    private val plane = getProperty("homePlane", 0)
    private val tile = Tile(x, y, plane)

    private val bus: EventBus = get()
    private val collisions: Collisions = get()
    private val definitions: ContainerDefinitions = get()
    private val interfaces: InterfaceDetails = get()

    private val fileIO: FileIO by inject()

    /**
     * Loads a player's file
     */
    fun loadPlayer(name: String): Player {
        val read = read(name)
        if (read == null) {
            logger.trace { "New player constructed" }
        }
        return bind(read)
    }

    private fun read(name: String): Player {
        val player: Player = fileIO.read("${name}.yml")
        logger.info { "read operation return [player=$player]" }
        return player
    }

    /**
     * Bind all necessary components of the player
     */
    fun bind(player: Player): Player {
        val interfaceIO = PlayerInterfaceIO(player, bus)
        player.interfaces = InterfaceManager(interfaceIO, interfaces, player.gameFrame)
        player.interfaceOptions = InterfaceOptions(player, interfaces, definitions)
        player.experience.addListener { skill, _, experience ->
            val level = player.levels.get(skill)
            player.send(SkillLevelMessage(skill.ordinal, level, experience.toInt()))
        }
        player.interactTarget = RectangleTargetStrategy(collisions, player)
        player.followTarget = FollowTargetStrategy(player)
        return player
    }

}

val playerIO = module { single(createdAtStart = true) { PlayerIO() } }