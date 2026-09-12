package server.net.login;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginDecoderTest {

    private static final long SERVER_SEED = 0x0102030405060708L;
    private static final long CLIENT_SEED = 0x1112131415161718L;

    @Test
    void decodesHandshakeAcrossFragments() {
        EmbeddedChannel channel = new EmbeddedChannel(new LoginDecoder(() -> SERVER_SEED));

        assertFalse(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{14})));
        assertNull(channel.readOutbound());

        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{5}));
        ByteBuf response = channel.readOutbound();

        assertEquals(17, response.readableBytes());
        for (int index = 0; index < 9; index++) {
            assertEquals(0, response.readUnsignedByte());
        }
        assertEquals(SERVER_SEED, response.readLong());

        response.release();
        channel.finishAndReleaseAll();
    }

    @Test
    void decodesProjectInsanityLoginPacket() {
        EmbeddedChannel channel = handshakenChannel();
        int[] crcs = {1, 2, 3, 4, 5, 6, 7, 8, 9};

        channel.writeInbound(loginPacket(317, SERVER_SEED, false, crcs, "test user", "CaseSensitive123"));
        LoginRequest request = channel.readInbound();

        assertEquals("test user", request.username());
        assertEquals("CaseSensitive123", request.password());
        assertEquals(999999, request.uid());
        assertFalse(request.reconnecting());
        assertFalse(request.lowMemory());
        assertArrayEquals(crcs, request.cacheCrcs());
        assertArrayEquals(new int[]{
                (int) (CLIENT_SEED >>> 32),
                (int) CLIENT_SEED,
                (int) (SERVER_SEED >>> 32),
                (int) SERVER_SEED
        }, request.isaacSeed());

        channel.finishAndReleaseAll();
    }

    @Test
    void rejectsWrongRevisionWithOutOfDateResponse() {
        EmbeddedChannel channel = handshakenChannel();

        channel.writeInbound(loginPacket(316, SERVER_SEED, false, new int[9], "test", "pass"));
        channel.runPendingTasks();

        ByteBuf response = channel.readOutbound();
        assertEquals(LoginResponse.OUT_OF_DATE.code(), response.readUnsignedByte());
        response.release();
        assertFalse(channel.isOpen());
        channel.finishAndReleaseAll();
    }

    @Test
    void rejectsWrongEchoedServerSeed() {
        EmbeddedChannel channel = handshakenChannel();

        channel.writeInbound(loginPacket(317, 1234L, false, new int[9], "test", "pass"));
        channel.runPendingTasks();

        assertFalse(channel.isOpen());
        assertNull(channel.readInbound());
        channel.finishAndReleaseAll();
    }

    @Test
    void acceptsReconnectFraming() {
        EmbeddedChannel channel = handshakenChannel();

        ByteBuf packet = loginPacket(317, SERVER_SEED, true, new int[9], "test", "pass");
        channel.writeInbound(packet);
        LoginRequest request = channel.readInbound();

        assertTrue(request.reconnecting());
        channel.finishAndReleaseAll();
    }

    private EmbeddedChannel handshakenChannel() {
        EmbeddedChannel channel = new EmbeddedChannel(new LoginDecoder(() -> SERVER_SEED));
        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{14, 0}));
        ByteBuf response = channel.readOutbound();
        response.release();
        return channel;
    }

    private ByteBuf loginPacket(
            int revision,
            long echoedServerSeed,
            boolean reconnect,
            int[] crcs,
            String username,
            String password
    ) {
        ByteBuf secure = Unpooled.buffer();
        secure.writeByte(10);
        secure.writeLong(CLIENT_SEED);
        secure.writeLong(echoedServerSeed);
        secure.writeInt(999999);
        writeString(secure, username);
        writeString(secure, password);

        ByteBuf payload = Unpooled.buffer();
        payload.writeByte(255);
        payload.writeShort(revision);
        payload.writeByte(0);
        for (int crc : crcs) {
            payload.writeInt(crc);
        }
        payload.writeByte(secure.readableBytes());
        payload.writeBytes(secure);
        secure.release();

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(reconnect ? 18 : 16);
        packet.writeByte(payload.readableBytes());
        packet.writeBytes(payload);
        payload.release();
        return packet;
    }

    private void writeString(ByteBuf buffer, String value) {
        buffer.writeCharSequence(value, java.nio.charset.StandardCharsets.ISO_8859_1);
        buffer.writeByte(10);
    }
}
