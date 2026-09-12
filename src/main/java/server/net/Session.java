package server.net;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.Objects;

public final class Session {

    static final AttributeKey<Session> KEY = AttributeKey.valueOf(Session.class, "session");

    private final Channel channel;

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
}
