package server.world;

import server.world.player.Player;
import server.world.player.PlayerList;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class World {

    private final PlayerList players = new PlayerList();
    private final Queue<Player> pendingPlayerAdditions = new ConcurrentLinkedQueue<>();
    private final Queue<Player> pendingPlayerRemovals = new ConcurrentLinkedQueue<>();

    private long cycle;

    public void cycle() {
        processPlayerChanges();
        cycle++;
    }

    public void registerPlayer(Player player) {
        Objects.requireNonNull(player, "player");

        pendingPlayerRemovals.remove(player);

        if (player.getIndex() == Player.NO_INDEX && !pendingPlayerAdditions.contains(player)) {
            pendingPlayerAdditions.add(player);
        }
    }

    public void unregisterPlayer(Player player) {
        Objects.requireNonNull(player, "player");

        pendingPlayerAdditions.remove(player);

        if (player.getIndex() != Player.NO_INDEX && !pendingPlayerRemovals.contains(player)) {
            pendingPlayerRemovals.add(player);
        }
    }

    public PlayerList getPlayers() {
        return players;
    }

    public long getCycle() {
        return cycle;
    }

    private void processPlayerChanges() {
        Player player;

        while ((player = pendingPlayerRemovals.poll()) != null) {
            players.remove(player);
        }

        while ((player = pendingPlayerAdditions.poll()) != null) {
            if (player.getIndex() == Player.NO_INDEX) {
                players.add(player);
            }
        }
    }
}
