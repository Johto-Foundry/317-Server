package server.net;

import java.util.Objects;

public final class Packet {

    private final int opcode;
    private final PacketType type;
    private final byte[] payload;

    public Packet(int opcode, PacketType type, byte[] payload) {
        if (opcode < 0 || opcode > 255) {
            throw new IllegalArgumentException("opcode out of range: " + opcode);
        }
        this.opcode = opcode;
        this.type = Objects.requireNonNull(type, "type");
        this.payload = Objects.requireNonNull(payload, "payload").clone();
    }

    public int opcode() {
        return opcode;
    }

    public PacketType type() {
        return type;
    }

    public byte[] payload() {
        return payload.clone();
    }

    byte[] rawPayload() {
        return payload;
    }
}
