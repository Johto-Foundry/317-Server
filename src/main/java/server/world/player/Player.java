package server.world.player;

import server.world.entity.Entity;
import server.world.map.Tile;

public final class Player extends Entity {

    private final String username;
    private final Appearance appearance = new Appearance();
    private int combatLevel = 3;

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

    public Appearance getAppearance() {
        return appearance;
    }

    public int getCombatLevel() {
        return combatLevel;
    }
}
