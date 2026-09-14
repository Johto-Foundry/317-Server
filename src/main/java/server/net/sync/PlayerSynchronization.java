package server.net.sync;

import server.net.Packet;
import server.net.PacketBuilder;
import server.net.PacketType;
import server.world.map.Tile;
import server.world.player.Appearance;
import server.world.player.Player;

public final class PlayerSynchronization {

    private static final int OPCODE = 81;
    private static final int PLAYER_LIST_TERMINATOR = 2047;
    private static final int APPEARANCE_MASK = 0x10;

    private static final int STAND_ANIMATION = 0x328;
    private static final int STAND_TURN_ANIMATION = 0x337;
    private static final int WALK_ANIMATION = 0x333;
    private static final int TURN_180_ANIMATION = 0x334;
    private static final int TURN_90_CLOCKWISE_ANIMATION = 0x335;
    private static final int TURN_90_COUNTER_CLOCKWISE_ANIMATION = 0x336;
    private static final int RUN_ANIMATION = 0x338;

    private PlayerSynchronization() {
    }

    public static Packet initial(Player player) {
        Tile tile = player.getTile();
        int centralRegionX = tile.x() >> 3;
        int centralRegionY = tile.y() >> 3;
        int baseX = (centralRegionX - 6) << 3;
        int baseY = (centralRegionY - 6) << 3;
        int localX = tile.x() - baseX;
        int localY = tile.y() - baseY;

        if (localX < 0 || localX > 127 || localY < 0 || localY > 127) {
            throw new IllegalStateException("Player is outside the local scene: " + tile);
        }

        byte[] appearance = appearance(player);
        PacketBuilder updates = new PacketBuilder()
                .writeByte(APPEARANCE_MASK)
                .writeByte(-appearance.length)
                .writeBytes(appearance);

        PacketBuilder payload = new PacketBuilder();
        payload.startBitAccess();
        payload.writeBits(1, 1);
        payload.writeBits(2, 3);
        payload.writeBits(2, tile.level());
        payload.writeBits(1, 1);
        payload.writeBits(1, 1);
        payload.writeBits(7, localY);
        payload.writeBits(7, localX);
        payload.writeBits(8, 0);
        payload.writeBits(11, PLAYER_LIST_TERMINATOR);
        payload.finishBitAccess();
        payload.writeBytes(updates.toByteArray());

        return new Packet(OPCODE, PacketType.VARIABLE_SHORT, payload.toByteArray());
    }

    private static byte[] appearance(Player player) {
        Appearance appearance = player.getAppearance();
        PacketBuilder properties = new PacketBuilder();

        properties.writeByte(appearance.getGender());
        properties.writeByte(255);
        properties.writeByte(0);
        properties.writeByte(0);
        properties.writeByte(0);
        properties.writeByte(0);
        properties.writeShort(0x100 + appearance.getChest());
        properties.writeByte(0);
        properties.writeShort(0x100 + appearance.getArms());
        properties.writeShort(0x100 + appearance.getLegs());
        properties.writeShort(0x100 + appearance.getHead());
        properties.writeShort(0x100 + appearance.getHands());
        properties.writeShort(0x100 + appearance.getFeet());
        properties.writeShort(0x100 + appearance.getBeard());

        properties.writeByte(appearance.getHairColor());
        properties.writeByte(appearance.getTorsoColor());
        properties.writeByte(appearance.getLegColor());
        properties.writeByte(appearance.getFeetColor());
        properties.writeByte(appearance.getSkinColor());

        properties.writeShort(STAND_ANIMATION);
        properties.writeShort(STAND_TURN_ANIMATION);
        properties.writeShort(WALK_ANIMATION);
        properties.writeShort(TURN_180_ANIMATION);
        properties.writeShort(TURN_90_CLOCKWISE_ANIMATION);
        properties.writeShort(TURN_90_COUNTER_CLOCKWISE_ANIMATION);
        properties.writeShort(RUN_ANIMATION);

        properties.writeLong(encodeName(player.getUsername()));
        properties.writeByte(player.getCombatLevel());
        properties.writeShort(0);

        return properties.toByteArray();
    }

    static long encodeName(String username) {
        if (username == null) {
            return 0L;
        }

        long encoded = 0L;
        int length = Math.min(username.length(), 12);
        for (int index = 0; index < length; index++) {
            char character = username.charAt(index);
            encoded *= 37L;

            if (character >= 'A' && character <= 'Z') {
                encoded += 1 + character - 'A';
            } else if (character >= 'a' && character <= 'z') {
                encoded += 1 + character - 'a';
            } else if (character >= '0' && character <= '9') {
                encoded += 27 + character - '0';
            }
        }

        while (encoded != 0L && encoded % 37L == 0L) {
            encoded /= 37L;
        }
        return encoded;
    }
}
