package org.redrune.network.codec.update.decode

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.update.UpdateMessageDecoder
import org.redrune.network.codec.update.message.FileRequestMessage
import org.redrune.network.codec.update.message.FileRequestType.FILE_REQUEST
import org.redrune.network.codec.update.message.FileRequestType.PRIORITY_FILE_REQUEST
import org.redrune.network.packet.PacketReader

/**
 * This class decodes the update protocol into its version different messages
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class FileRequestDecoder : UpdateMessageDecoder<FileRequestMessage>(3, FILE_REQUEST.opcode, PRIORITY_FILE_REQUEST.opcode) {

    override fun decode(reader: PacketReader, ctx: ChannelHandlerContext): FileRequestMessage {
        val indexId: Int = reader.readUnsignedByte()
        val archiveId: Int = reader.readUnsignedShort()
        return FileRequestMessage(indexId, archiveId, reader.opcode == 1)
    }

}