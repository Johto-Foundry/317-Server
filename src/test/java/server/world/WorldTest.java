package server.world;

import org.junit.jupiter.api.Test;
import server.world.player.Player;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        assertEquals(0, world.getPlayers().size());
        assertEquals(Player.NO_INDEX, player.getIndex());

        world.cycle();

        assertEquals(1, world.getPlayers().size());
        assertEquals(1, player.getIndex());
        assertSame(player, world.getPlayers().get(1));

        world.unregisterPlayer(player);
        assertEquals(1, world.getPlayers().size());

        world.cycle();

        assertEquals(0, world.getPlayers().size());
        assertEquals(Player.NO_INDEX, player.getIndex());
    }

    @Test
    void cancelsRegistrationBeforePlayerBecomesActive() {
        World world = new World();
        Player player = new Player();

        world.registerPlayer(player);
        world.unregisterPlayer(player);
        world.cycle();

        assertEquals(0, world.getPlayers().size());
        assertEquals(Player.NO_INDEX, player.getIndex());
    }
}
