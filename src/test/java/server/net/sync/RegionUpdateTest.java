package server.net.sync;

import org.junit.jupiter.api.Test;
import server.net.Packet;
import server.net.PacketType;
import server.world.map.Tile;
import server.world.player.Player;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegionUpdateTest {

    @Test
    void encodesLumbridgeRegionUsing317Packet73Layout() {
        Player player = new Player("test", new Tile(3222, 3218, 0));

        Packet packet = RegionUpdate.create(player);

        assertEquals(73, packet.opcode());
        assertEquals(PacketType.FIXED, packet.type());
        assertArrayEquals(new byte[]{0x01, 0x12, 0x01, (byte) 0x92}, packet.payload());
    }
}
