package server.world.player;

public final class Player {

    public static final int NO_INDEX = -1;

    private int index = NO_INDEX;

    public int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }
}
