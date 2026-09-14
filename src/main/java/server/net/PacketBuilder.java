package server.net;

import java.util.Arrays;

public final class PacketBuilder {

    private byte[] buffer = new byte[64];
    private int position;
    private int bitPosition = -1;

    public int length() {
        return bitPosition == -1 ? position : (bitPosition + 7) / 8;
    }

    public PacketBuilder writeByte(int value) {
        requireByteAccess();
        ensureCapacity(position + 1);
        buffer[position++] = (byte) value;
        return this;
    }

    public PacketBuilder writeShort(int value) {
        requireByteAccess();
        ensureCapacity(position + 2);
        buffer[position++] = (byte) (value >>> 8);
        buffer[position++] = (byte) value;
        return this;
    }

    public PacketBuilder writeShortA(int value) {
        requireByteAccess();
        ensureCapacity(position + 2);
        buffer[position++] = (byte) (value >>> 8);
        buffer[position++] = (byte) (value + 128);
        return this;
    }

    public PacketBuilder writeLong(long value) {
        requireByteAccess();
        ensureCapacity(position + 8);
        for (int shift = 56; shift >= 0; shift -= 8) {
            buffer[position++] = (byte) (value >>> shift);
        }
        return this;
    }

    public PacketBuilder writeBytes(byte[] values) {
        requireByteAccess();
        ensureCapacity(position + values.length);
        System.arraycopy(values, 0, buffer, position, values.length);
        position += values.length;
        return this;
    }

    public PacketBuilder startBitAccess() {
        if (bitPosition != -1) {
            throw new IllegalStateException("Already in bit access mode.");
        }
        bitPosition = position * 8;
        return this;
    }

    public PacketBuilder writeBits(int count, int value) {
        if (bitPosition == -1) {
            throw new IllegalStateException("Not in bit access mode.");
        }
        if (count < 0 || count > 32) {
            throw new IllegalArgumentException("bit count out of range: " + count);
        }
        if (count == 0) {
            return this;
        }

        ensureCapacity((bitPosition + count + 7) / 8);
        for (int bit = count - 1; bit >= 0; bit--) {
            int byteIndex = bitPosition >>> 3;
            int bitIndex = 7 - (bitPosition & 7);
            if (((value >>> bit) & 1) != 0) {
                buffer[byteIndex] |= (byte) (1 << bitIndex);
            }
            bitPosition++;
        }
        return this;
    }

    public PacketBuilder finishBitAccess() {
        if (bitPosition == -1) {
            throw new IllegalStateException("Not in bit access mode.");
        }
        position = (bitPosition + 7) / 8;
        bitPosition = -1;
        return this;
    }

    public byte[] toByteArray() {
        if (bitPosition != -1) {
            throw new IllegalStateException("Finish bit access before reading the payload.");
        }
        return Arrays.copyOf(buffer, position);
    }

    private void requireByteAccess() {
        if (bitPosition != -1) {
            throw new IllegalStateException("Finish bit access before writing bytes.");
        }
    }

    private void ensureCapacity(int required) {
        if (required <= buffer.length) {
            return;
        }
        int capacity = buffer.length;
        while (capacity < required) {
            capacity *= 2;
        }
        buffer = Arrays.copyOf(buffer, capacity);
    }
}
