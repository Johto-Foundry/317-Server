package server.world.player;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class PlayerList implements Iterable<Player> {

    public static final int MAX_PLAYERS = 2046;

    private final Player[] players = new Player[MAX_PLAYERS + 1];

    private int size;
    private int lastIndex;

    public boolean add(Player player) {
        Objects.requireNonNull(player, "player");

        if (player.getIndex() != Player.NO_INDEX) {
            throw new IllegalStateException("Player is already in a player list.");
        }

        int index = nextFreeIndex();
        if (index == Player.NO_INDEX) {
            return false;
        }

        players[index] = player;
        player.setIndex(index);
        lastIndex = index;
        size++;
        return true;
    }

    public boolean remove(Player player) {
        Objects.requireNonNull(player, "player");

        int index = player.getIndex();
        if (!validIndex(index) || players[index] != player) {
            return false;
        }

        players[index] = null;
        player.setIndex(Player.NO_INDEX);
        size--;
        return true;
    }

    public Player get(int index) {
        checkIndex(index);
        return players[index];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == MAX_PLAYERS;
    }

    private int nextFreeIndex() {
        for (int index = lastIndex + 1; index <= MAX_PLAYERS; index++) {
            if (players[index] == null) {
                return index;
            }
        }

        for (int index = 1; index <= lastIndex; index++) {
            if (players[index] == null) {
                return index;
            }
        }

        return Player.NO_INDEX;
    }

    private boolean validIndex(int index) {
        return index >= 1 && index <= MAX_PLAYERS;
    }

    private void checkIndex(int index) {
        if (!validIndex(index)) {
            throw new IndexOutOfBoundsException("Player index out of bounds: " + index);
        }
    }

    @Override
    public Iterator<Player> iterator() {
        return new Iterator<>() {

            private int index = 1;

            @Override
            public boolean hasNext() {
                while (index <= MAX_PLAYERS && players[index] == null) {
                    index++;
                }
                return index <= MAX_PLAYERS;
            }

            @Override
            public Player next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return players[index++];
            }
        };
    }
}
