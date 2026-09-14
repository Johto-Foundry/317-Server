package server.net.sync;

import org.junit.jupiter.api.Test;
import server.net.Packet;
import server.net.PacketType;
import server.world.map.Tile;
import server.world.player.Player;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerSynchronizationTest {

    @Test
    void encodesInitial317PlayerSynchronization() {
        Player player = new Player("test", new Tile(3222, 3218, 0));

        Packet packet = PlayerSynchronization.initial(player);

        assertEquals(81, packet.opcode());
        assertEquals(PacketType.VARIABLE_SHORT, packet.type());
        assertArrayEquals(
                HexFormat.of().parseHex(
                        "e6c9b007ff10cd00ff00000000011200011a012401000121012a010a" +
                        "0708090500032803370333033403350336033800000000000f92d4030000"
                ),
                packet.payload()
        );
    }

    @Test
    void encodes317Base37Names() {
        assertEquals(1020628L, PlayerSynchronization.encodeName("test"));
        assertEquals(PlayerSynchronization.encodeName("test user"), PlayerSynchronization.encodeName("TEST USER"));
    }
}
