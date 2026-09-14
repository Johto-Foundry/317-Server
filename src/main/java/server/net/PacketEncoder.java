package server.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.Objects;

public final class PacketEncoder extends MessageToByteEncoder<Packet> {

    private final Session session;

    public PacketEncoder(Session session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    @Override
    protected void encode(ChannelHandlerContext context, Packet packet, ByteBuf output) {
        byte[] payload = packet.rawPayload();

        output.writeByte(session.encodeOpcode(packet.opcode()));
        switch (packet.type()) {
            case FIXED -> {
            }
            case VARIABLE_BYTE -> {
                if (payload.length > 255) {
                    throw new IllegalArgumentException("Variable-byte packet is too large: " + payload.length);
                }
                output.writeByte(payload.length);
            }
            case VARIABLE_SHORT -> {
                if (payload.length > 65535) {
                    throw new IllegalArgumentException("Variable-short packet is too large: " + payload.length);
                }
                output.writeShort(payload.length);
            }
        }
        output.writeBytes(payload);
    }
}
