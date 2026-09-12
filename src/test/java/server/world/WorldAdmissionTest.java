package server.world;

import org.junit.jupiter.api.Test;
import server.world.map.Tile;
import server.world.player.Player;
import server.world.player.PlayerAdmission;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WorldAdmissionTest {

    @Test
    void admitsPlayerOnWorldCycle() {
        World world = new World();
        Player player = new Player("test", new Tile(3222, 3218));

        CompletableFuture<PlayerAdmission> admission = world.admitPlayer(player);
        assertFalse(admission.isDone());

        world.cycle();

        assertEquals(PlayerAdmission.ACCEPTED, admission.join());
        assertEquals(1, world.getPlayerCount());
        assertEquals(player, world.getPlayer(player.getIndex()));
    }

    @Test
    void duplicateUsernameIsRejectedAtomically() {
        World world = new World();
        Player first = new Player("test", new Tile(3222, 3218));
        Player second = new Player("TEST", new Tile(3222, 3218));

        CompletableFuture<PlayerAdmission> firstAdmission = world.admitPlayer(first);
        CompletableFuture<PlayerAdmission> secondAdmission = world.admitPlayer(second);
        world.cycle();

        assertEquals(PlayerAdmission.ACCEPTED, firstAdmission.join());
        assertEquals(PlayerAdmission.ALREADY_ONLINE, secondAdmission.join());
        assertEquals(1, world.getPlayerCount());
    }
}
