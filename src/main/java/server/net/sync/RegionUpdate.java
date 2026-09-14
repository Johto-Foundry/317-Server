package server.net.sync;

import server.net.Packet;
import server.net.PacketBuilder;
import server.net.PacketType;
import server.world.map.Tile;
import server.world.player.Player;

public final class RegionUpdate {

    private static final int OPCODE = 73;

    private RegionUpdate() {
    }

    public static Packet create(Player player) {
        Tile tile = player.getTile();
        int centralRegionX = tile.x() >> 3;
        int centralRegionY = tile.y() >> 3;

        PacketBuilder payload = new PacketBuilder()
                .writeShortA(centralRegionX)
                .writeShort(centralRegionY);

        return new Packet(OPCODE, PacketType.FIXED, payload.toByteArray());
    }
}
