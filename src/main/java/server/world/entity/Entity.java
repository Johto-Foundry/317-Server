package server.world.entity;

import server.world.map.Tile;

import java.util.Objects;

public abstract class Entity {

    public static final int NO_INDEX = -1;

    private int index = NO_INDEX;
    private Tile tile;

    protected Entity(Tile tile) {
        this.tile = Objects.requireNonNull(tile, "tile");
    }

    public final int getIndex() {
        return index;
    }

    public final Tile getTile() {
        return tile;
    }

    final void setIndex(int index) {
        this.index = index;
    }
}
