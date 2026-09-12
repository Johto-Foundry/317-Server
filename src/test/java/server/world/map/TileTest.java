package server.world.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TileTest {

    @Test
    void defaultsToGroundLevel() {
        assertEquals(new Tile(3222, 3218, 0), new Tile(3222, 3218));
    }

    @Test
    void acceptsCoordinateLimits() {
        new Tile(0, 0, 0);
        new Tile(Tile.MAX_COORDINATE, Tile.MAX_COORDINATE, Tile.MAX_LEVEL);
    }

    @Test
    void rejectsCoordinatesOutsideWorldLimits() {
        assertThrows(IllegalArgumentException.class, () -> new Tile(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Tile(Tile.MAX_COORDINATE + 1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, Tile.MAX_COORDINATE + 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, 0, Tile.MAX_LEVEL + 1));
    }

    @Test
    void translatesToNewTile() {
        Tile tile = new Tile(3200, 3200, 1);
        Tile translated = tile.translate(4, -3, 1);

        assertEquals(new Tile(3204, 3197, 2), translated);
        assertEquals(new Tile(3200, 3200, 1), tile);
        assertNotSame(tile, translated);
    }

    @Test
    void translatesWithoutChangingLevel() {
        Tile tile = new Tile(3200, 3200, 2);

        assertEquals(new Tile(3201, 3199, 2), tile.translate(1, -1));
    }
}
