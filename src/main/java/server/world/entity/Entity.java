package server.world.entity;

public abstract class Entity {

    public static final int NO_INDEX = -1;

    private int index = NO_INDEX;

    public final int getIndex() {
        return index;
    }

    final void setIndex(int index) {
        this.index = index;
    }
}
