package server.net;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTest {

    @Test
    void writesToChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);
        Object message = new Object();

        session.write(message);

        assertSame(message, channel.readOutbound());
        channel.finishAndReleaseAll();
    }

    @Test
    void closesChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        Session session = new Session(channel);

        assertTrue(session.isOpen());

        session.close();

        assertFalse(session.isOpen());
        channel.finishAndReleaseAll();
    }
}
