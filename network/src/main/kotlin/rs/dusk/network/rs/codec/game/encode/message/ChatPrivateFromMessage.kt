package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Jacob Rhiel <jacob.rhiel@gmail.com>
 * @created Oct 30, 2020 
 */
data class ChatPrivateFromMessage(val name: String, val rights: Int, val text: String) : Message