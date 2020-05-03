package rs.dusk.engine.client

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 31, 2020
 */
@Suppress("USELESS_CAST")
val clientLoginQueueModule = module {
    single { PlayerLoginQueue() as LoginQueue }
}

class PlayerLoginQueue : LoginQueue {

    private val factory: PlayerFactory by inject()
    private val logger = InlineLogger()

    override suspend fun add(username: String, session: Session?): LoginResponse {
        try {
            val player = factory.spawn(username, session).await() ?: return LoginResponse.Full
            return LoginResponse.Success(player)
        } catch (e: IllegalStateException) {
            logger.error(e) { "Error loading player $username" }
            return LoginResponse.Failure
        }
    }

}