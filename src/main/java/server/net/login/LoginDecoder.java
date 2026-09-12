package server.net.login;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.security.SecureRandom;
import java.util.List;
import java.util.function.LongSupplier;

public final class LoginDecoder extends ByteToMessageDecoder {

    private static final int HANDSHAKE_OPCODE = 14;
    private static final int NORMAL_LOGIN = 16;
    private static final int RECONNECT_LOGIN = 18;
    private static final int MAGIC = 255;
    private static final int REVISION = 317;
    private static final int CACHE_CRC_COUNT = 9;
    private static final int LOGIN_FIXED_SIZE = 40;
    private static final int SECURE_BLOCK_ID = 10;

    private final LongSupplier seedGenerator;

    private State state = State.HANDSHAKE;
    private long serverSeed;
    private int loginType;
    private int loginPacketSize;

    public LoginDecoder() {
        SecureRandom random = new SecureRandom();
        this.seedGenerator = random::nextLong;
    }

    LoginDecoder(LongSupplier seedGenerator) {
        this.seedGenerator = seedGenerator;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> output) {
        switch (state) {
            case HANDSHAKE -> decodeHandshake(context, buffer);
            case LOGIN_HEADER -> decodeLoginHeader(context, buffer);
            case LOGIN_PAYLOAD -> decodeLoginPayload(context, buffer, output);
            case COMPLETE -> {
            }
        }
    }

    private void decodeHandshake(ChannelHandlerContext context, ByteBuf buffer) {
        if (buffer.readableBytes() < 2) {
            return;
        }

        int opcode = buffer.readUnsignedByte();
        buffer.readUnsignedByte();

        if (opcode != HANDSHAKE_OPCODE) {
            context.close();
            return;
        }

        serverSeed = seedGenerator.getAsLong();

        ByteBuf response = context.alloc().buffer(17);
        response.writeZero(8);
        response.writeByte(0);
        response.writeLong(serverSeed);
        context.writeAndFlush(response);

        state = State.LOGIN_HEADER;
    }

    private void decodeLoginHeader(ChannelHandlerContext context, ByteBuf buffer) {
        if (buffer.readableBytes() < 2) {
            return;
        }

        loginType = buffer.readUnsignedByte();
        loginPacketSize = buffer.readUnsignedByte();

        if (loginType != NORMAL_LOGIN && loginType != RECONNECT_LOGIN) {
            context.close();
            return;
        }
        if (loginPacketSize <= LOGIN_FIXED_SIZE) {
            context.close();
            return;
        }

        state = State.LOGIN_PAYLOAD;
    }

    private void decodeLoginPayload(ChannelHandlerContext context, ByteBuf buffer, List<Object> output) {
        if (buffer.readableBytes() < loginPacketSize) {
            return;
        }

        ByteBuf payload = buffer.readSlice(loginPacketSize);

        if (payload.readUnsignedByte() != MAGIC) {
            context.close();
            return;
        }

        int revision = payload.readUnsignedShort();
        if (revision != REVISION) {
            reject(context, LoginResponse.OUT_OF_DATE);
            return;
        }

        boolean lowMemory = payload.readUnsignedByte() == 1;
        int[] cacheCrcs = new int[CACHE_CRC_COUNT];
        for (int index = 0; index < cacheCrcs.length; index++) {
            cacheCrcs[index] = payload.readInt();
        }

        if (!payload.isReadable()) {
            context.close();
            return;
        }

        int secureBlockLength = payload.readUnsignedByte();
        if (secureBlockLength != payload.readableBytes() || secureBlockLength < 23) {
            context.close();
            return;
        }

        if (payload.readUnsignedByte() != SECURE_BLOCK_ID) {
            context.close();
            return;
        }

        long clientSeed = payload.readLong();
        long echoedServerSeed = payload.readLong();
        if (echoedServerSeed != serverSeed) {
            context.close();
            return;
        }

        int uid = payload.readInt();
        String username = readString(payload, 12);
        String password = readString(payload, 64);

        if (username == null || password == null || payload.isReadable()) {
            context.close();
            return;
        }

        int[] isaacSeed = {
                (int) (clientSeed >>> 32),
                (int) clientSeed,
                (int) (serverSeed >>> 32),
                (int) serverSeed
        };

        output.add(new LoginRequest(
                username,
                password,
                uid,
                loginType == RECONNECT_LOGIN,
                lowMemory,
                cacheCrcs,
                isaacSeed
        ));
        state = State.COMPLETE;
    }

    private String readString(ByteBuf buffer, int maximumLength) {
        int start = buffer.readerIndex();
        int length = 0;

        while (buffer.isReadable() && length <= maximumLength) {
            if (buffer.readByte() == 10) {
                return buffer.toString(start, length, java.nio.charset.StandardCharsets.ISO_8859_1);
            }
            length++;
        }

        return null;
    }

    private void reject(ChannelHandlerContext context, LoginResponse response) {
        context.writeAndFlush(context.alloc().buffer(1).writeByte(response.code()))
                .addListener(future -> context.close());
        state = State.COMPLETE;
    }

    private enum State {
        HANDSHAKE,
        LOGIN_HEADER,
        LOGIN_PAYLOAD,
        COMPLETE
    }
}
