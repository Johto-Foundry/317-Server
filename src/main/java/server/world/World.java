package server.world;

import server.world.entity.Entity;
import server.world.npc.Npc;
import server.world.npc.NpcList;
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
    private final NpcList npcs = new NpcList();
    private final Queue<PlayerChange> pendingPlayerChanges = new ConcurrentLinkedQueue<>();
    private final Queue<NpcChange> pendingNpcChanges = new ConcurrentLinkedQueue<>();

    private long cycle;

    public void cycle() {
        processPlayerChanges();
        processNpcChanges();
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

    public void registerNpc(Npc npc) {
        pendingNpcChanges.add(new NpcChange(Objects.requireNonNull(npc, "npc"), true));
    }

    public void unregisterNpc(Npc npc) {
        pendingNpcChanges.add(new NpcChange(Objects.requireNonNull(npc, "npc"), false));
    }

    public Npc getNpc(int index) {
        return npcs.get(index);
    }

    public int getNpcCount() {
        return npcs.size();
    }

    public Iterable<Npc> getNpcs() {
        return npcs::iterator;
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
                if (player.getIndex() == Entity.NO_INDEX) {
                    players.add(player);
                }
            } else if (player.getIndex() != Entity.NO_INDEX) {
                players.remove(player);
            }
        }
    }

    private void processNpcChanges() {
        Map<Npc, Boolean> desiredState = new IdentityHashMap<>();
        List<Npc> order = new ArrayList<>();
        NpcChange change;

        while ((change = pendingNpcChanges.poll()) != null) {
            Npc npc = change.npc();
            if (!desiredState.containsKey(npc)) {
                order.add(npc);
            }
            desiredState.put(npc, change.registered());
        }

        for (Npc npc : order) {
            boolean shouldBeRegistered = desiredState.get(npc);

            if (shouldBeRegistered) {
                if (npc.getIndex() == Entity.NO_INDEX) {
                    npcs.add(npc);
                }
            } else if (npc.getIndex() != Entity.NO_INDEX) {
                npcs.remove(npc);
            }
        }
    }

    private record PlayerChange(Player player, boolean registered) {
    }

    private record NpcChange(Npc npc, boolean registered) {
    }
}
