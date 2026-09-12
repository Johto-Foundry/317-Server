package server.world.player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerListTest {

    @Test
    void startsPlayersAtIndexOne() {
        PlayerList players = new PlayerList();
        Player player = new Player();

        assertTrue(players.add(player));
        assertEquals(1, player.getIndex());
        assertSame(player, players.get(1));
    }

    @Test
    void givesPlayersUniqueIndexes() {
        PlayerList players = new PlayerList();
        Player first = new Player();
        Player second = new Player();

        players.add(first);
        players.add(second);

        assertEquals(1, first.getIndex());
        assertEquals(2, second.getIndex());
        assertEquals(2, players.size());
    }

    @Test
    void removesPlayerAndClearsIndex() {
        PlayerList players = new PlayerList();
        Player player = new Player();
        players.add(player);
        int index = player.getIndex();

        assertTrue(players.remove(player));
        assertEquals(Player.NO_INDEX, player.getIndex());
        assertNull(players.get(index));
        assertTrue(players.isEmpty());
    }

    @Test
    void doesNotRemoveDifferentPlayerWithSameIndex() {
        PlayerList players = new PlayerList();
        PlayerList otherPlayers = new PlayerList();
        Player player = new Player();
        Player stalePlayer = new Player();
        players.add(player);
        otherPlayers.add(stalePlayer);

        assertEquals(player.getIndex(), stalePlayer.getIndex());
        assertFalse(players.remove(stalePlayer));
        assertSame(player, players.get(player.getIndex()));
        assertEquals(1, players.size());
    }

    @Test
    void iteratesOverActivePlayersOnly() {
        PlayerList players = new PlayerList();
        Player first = new Player();
        Player second = new Player();
        Player third = new Player();
        players.add(first);
        players.add(second);
        players.add(third);
        players.remove(second);

        List<Player> found = new ArrayList<>();
        players.forEach(found::add);

        assertEquals(List.of(first, third), found);
    }

    @Test
    void reservesProtocolIndexes() {
        PlayerList players = new PlayerList();

        assertThrows(IndexOutOfBoundsException.class, () -> players.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> players.get(2047));
    }

    @Test
    void reusesFreeIndexAfterWrapping() {
        PlayerList players = new PlayerList();
        Player freedPlayer = null;

        for (int index = 1; index <= PlayerList.MAX_PLAYERS; index++) {
            Player player = new Player();
            assertTrue(players.add(player));
            if (index == 1000) {
                freedPlayer = player;
            }
        }

        assertTrue(players.isFull());
        assertFalse(players.add(new Player()));

        int freedIndex = freedPlayer.getIndex();
        assertTrue(players.remove(freedPlayer));

        Player replacement = new Player();
        assertTrue(players.add(replacement));
        assertEquals(freedIndex, replacement.getIndex());
    }
}
