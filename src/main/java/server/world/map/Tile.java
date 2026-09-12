package server.world.map;

public record Tile(int x, int y, int level) {

    public static final int MAX_COORDINATE = 16383;
    public static final int MAX_LEVEL = 3;

    public Tile {
        if (x < 0 || x > MAX_COORDINATE) {
            throw new IllegalArgumentException("x out of bounds: " + x);
        }
        if (y < 0 || y > MAX_COORDINATE) {
            throw new IllegalArgumentException("y out of bounds: " + y);
        }
        if (level < 0 || level > MAX_LEVEL) {
            throw new IllegalArgumentException("level out of bounds: " + level);
        }
    }

    public Tile(int x, int y) {
        this(x, y, 0);
    }

    public Tile translate(int deltaX, int deltaY) {
        return translate(deltaX, deltaY, 0);
    }

    public Tile translate(int deltaX, int deltaY, int deltaLevel) {
        return new Tile(x + deltaX, y + deltaY, level + deltaLevel);
    }
}
