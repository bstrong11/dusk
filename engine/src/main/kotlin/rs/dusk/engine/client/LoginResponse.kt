package rs.dusk.engine.client

import rs.dusk.engine.entity.model.Player

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
sealed class LoginResponse(val code: Int) {
    object Full : LoginResponse(7)
    data class Success(val player: Player) : LoginResponse(2)
    object Failure : LoginResponse(13)
}