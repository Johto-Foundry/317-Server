package server.world;

import server.world.player.Player;
import server.world.player.PlayerList;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class World {

    private final PlayerList players = new PlayerList();
    private final Queue<PlayerChange> pendingPlayerChanges = new ConcurrentLinkedQueue<>();

    private long cycle;

    public void cycle() {
        processPlayerChanges();
        cycle++;
    }

    public void registerPlayer(Player player) {
        pendingPlayerChanges.add(new PlayerChange(Objects.requireNonNull(player, "player"), true));
    }

    public void unregisterPlayer(Player player) {
        pendingPlayerChanges.add(new PlayerChange(Objects.requireNonNull(player, "player"), false));
    }

    public Player getPlayer(int index) {
        return players.get(index);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public Iterable<Player> getPlayers() {
        return players::iterator;
    }

    public long getCycle() {
        return cycle;
    }

    private void processPlayerChanges() {
        Map<Player, Boolean> desiredState = new IdentityHashMap<>();
        List<Player> order = new ArrayList<>();
        PlayerChange change;

        while ((change = pendingPlayerChanges.poll()) != null) {
            Player player = change.player();
            if (!desiredState.containsKey(player)) {
                order.add(player);
            }
            desiredState.put(player, change.registered());
        }

        for (Player player : order) {
            boolean shouldBeRegistered = desiredState.get(player);

            if (shouldBeRegistered) {
                if (player.getIndex() == Player.NO_INDEX) {
                    players.add(player);
                }
            } else if (player.getIndex() != Player.NO_INDEX) {
                players.remove(player);
            }
        }
    }

    private record PlayerChange(Player player, boolean registered) {
    }
}
