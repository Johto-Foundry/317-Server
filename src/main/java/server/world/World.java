package server.world;

public final class World {

    private long cycle;

    public void cycle() {
        cycle++;
    }

    public long getCycle() {
        return cycle;
    }
}
