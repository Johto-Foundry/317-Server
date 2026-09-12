package server.net;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import server.world.player.Player;

import java.util.Objects;

public final class Session {

    static final AttributeKey<Session> KEY = AttributeKey.valueOf(Session.class, "session");

    private final Channel channel;

    private Player player;
    private IsaacCipher incomingCipher;
    private IsaacCipher outgoingCipher;

    Session(Channel channel) {
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    public void write(Object message) {
        channel.writeAndFlush(Objects.requireNonNull(message, "message"));
    }

    public void close() {
        channel.close();
    }

    public Player getPlayer() {
        return player;
    }

    public void attachPlayer(Player player) {
        if (this.player != null) {
            throw new IllegalStateException("Session already has a player.");
        }
        this.player = Objects.requireNonNull(player, "player");
    }

    public void initializeIsaac(int[] seed) {
        int[] incomingSeed = seed.clone();
        int[] outgoingSeed = seed.clone();
        for (int index = 0; index < outgoingSeed.length; index++) {
            outgoingSeed[index] += 50;
        }

        incomingCipher = new IsaacCipher(incomingSeed);
        outgoingCipher = new IsaacCipher(outgoingSeed);
    }

    int decodeOpcode(int opcode) {
        if (incomingCipher == null) {
            throw new IllegalStateException("ISAAC has not been initialized.");
        }
        return (opcode - incomingCipher.nextInt()) & 0xff;
    }

    int encodeOpcode(int opcode) {
        if (outgoingCipher == null) {
            throw new IllegalStateException("ISAAC has not been initialized.");
        }
        return (opcode + outgoingCipher.nextInt()) & 0xff;
    }
}
