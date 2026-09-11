package server.world;

import org.junit.jupiter.api.Test;
import server.world.player.Player;
import server.world.player.PlayerList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class WorldTest {

    @Test
    void advancesOneCycleAtATime() {
        World world = new World();

        assertEquals(0, world.getCycle());

        world.cycle();
        assertEquals(1, world.getCycle());

        world.cycle();
        assertEquals(2, world.getCycle());
    }

    @Test
    void appliesPlayerChangesAtCycleBoundary() {
        World world = new World();
        Player player = new Player();

        world.registerPlayer(player);

        assertEquals(0, world.getPlayerCount());
        assertEquals(Player.NO_INDEX, player.getIndex());

        world.cycle();

        assertEquals(1, world.getPlayerCount());
        assertEquals(1, player.getIndex());
        assertSame(player, world.getPlayer(1));

        world.unregisterPlayer(player);
        assertEquals(1, world.getPlayerCount());

        world.cycle();

        assertEquals(0, world.getPlayerCount());
        assertEquals(Player.NO_INDEX, player.getIndex());
    }

    @Test
    void usesLastPlayerChangeBeforeCycle() {
        World world = new World();
        Player player = new Player();

        world.registerPlayer(player);
        world.unregisterPlayer(player);
        world.registerPlayer(player);
        world.cycle();

        assertEquals(1, world.getPlayerCount());
        assertSame(player, world.getPlayer(player.getIndex()));
    }

    @Test
    void doesNotReindexActivePlayerWhenRemovalIsCancelled() {
        World world = new World();
        Player player = new Player();

        world.registerPlayer(player);
        world.cycle();
        int index = player.getIndex();

        world.unregisterPlayer(player);
        world.registerPlayer(player);
        world.cycle();

        assertEquals(index, player.getIndex());
        assertEquals(1, world.getPlayerCount());
        assertSame(player, world.getPlayer(index));
    }

    @Test
    void cancelsRegistrationBeforePlayerBecomesActive() {
        World world = new World();
        Player player = new Player();

        world.registerPlayer(player);
        world.unregisterPlayer(player);
        world.cycle();

        assertEquals(0, world.getPlayerCount());
        assertEquals(Player.NO_INDEX, player.getIndex());
    }

    @Test
    void doesNotExposeMutablePlayerList() {
        World world = new World();

        assertFalse(world.getPlayers() instanceof PlayerList);
    }
}
