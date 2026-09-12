package server.world.player;

import server.world.entity.Entity;
import server.world.map.Tile;

public final class Player extends Entity {

    private final String username;

    public Player(Tile tile) {
        this(null, tile);
    }

    public Player(String username, Tile tile) {
        super(tile);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
