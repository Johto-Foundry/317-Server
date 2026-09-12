package server.world.player;

import server.world.entity.EntityList;

public final class PlayerList extends EntityList<Player> {

    public static final int MAX_PLAYERS = 2046;

    public PlayerList() {
        super(MAX_PLAYERS);
    }
}
