package server.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
