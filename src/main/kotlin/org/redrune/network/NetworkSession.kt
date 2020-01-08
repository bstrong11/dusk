package org.redrune.network

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import org.redrune.network.packet.struct.OutgoingPacket

/**
 * This class represents the network session from the client (player) to the server
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class NetworkSession(
    /**
     * The channel for the connection
     */
    var channel: Channel
) {

    /**
     * Writes a packet to the channel
     * @return ChannelFuture
     */
    fun write(packet: OutgoingPacket, flush: Boolean = true): ChannelFuture {
        return if (flush) channel.writeAndFlush(packet) else channel.write(packet)
    }

}