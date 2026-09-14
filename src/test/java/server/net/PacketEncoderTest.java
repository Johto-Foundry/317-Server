package server.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacketEncoderTest {

    @Test
    void encryptsOpcodeAndWritesVariableShortLength() {
        int[] seed = {1, 2, 3, 4};

        EmbeddedChannel sessionChannel = new EmbeddedChannel();
        Session session = new Session(sessionChannel);
        session.initializeIsaac(seed);

        EmbeddedChannel expectedChannel = new EmbeddedChannel();
        Session expected = new Session(expectedChannel);
        expected.initializeIsaac(seed);
        int expectedOpcode = expected.encodeOpcode(81);

        EmbeddedChannel encoderChannel = new EmbeddedChannel(new PacketEncoder(session));
        Packet packet = new Packet(81, PacketType.VARIABLE_SHORT, new byte[]{1, 2, 3, 4});

        assertTrue(encoderChannel.writeOutbound(packet));
        ByteBuf encoded = encoderChannel.readOutbound();

        assertEquals(expectedOpcode, encoded.readUnsignedByte());
        assertEquals(4, encoded.readUnsignedShort());
        byte[] payload = new byte[4];
        encoded.readBytes(payload);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, payload);

        encoded.release();
        encoderChannel.finishAndReleaseAll();
        sessionChannel.finishAndReleaseAll();
        expectedChannel.finishAndReleaseAll();
    }
}
