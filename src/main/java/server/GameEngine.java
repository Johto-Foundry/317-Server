package server;

import server.world.World;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public final class GameEngine implements Runnable {

    public static final long CYCLE_LENGTH_MILLIS = 600L;

    private final World world;

    private volatile boolean running;
    private Thread thread;
    private long excessCycleNanos;

    public GameEngine(World world) {
        this.world = world;
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;
        thread = Thread.ofPlatform()
                .name("GameEngine")
                .start(this);
    }

    public synchronized void stop() {
        running = false;

        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        while (running) {
            long cycleStart = System.nanoTime();
            world.cycle();

            long elapsedNanos = System.nanoTime() - cycleStart + excessCycleNanos;
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

            if (elapsedMillis > CYCLE_LENGTH_MILLIS) {
                System.err.printf(
                        "Cycle %d took %dms%n",
                        world.getCycle(),
                        elapsedMillis
                );
            }

            excessCycleNanos = elapsedNanos - TimeUnit.MILLISECONDS.toNanos(elapsedMillis);
            waitFor(calculateWaitMillis(elapsedNanos));
        }
    }

    static long calculateWaitMillis(long elapsedNanos) {
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

        if (elapsedMillis > CYCLE_LENGTH_MILLIS) {
            long elapsedCycles = elapsedMillis / CYCLE_LENGTH_MILLIS;
            long nextBoundary = (elapsedCycles + 1) * CYCLE_LENGTH_MILLIS;
            return nextBoundary - elapsedMillis;
        }

        return CYCLE_LENGTH_MILLIS - elapsedMillis;
    }

    private void waitFor(long delayMillis) {
        long targetTime = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(delayMillis);

        while (running) {
            long remaining = targetTime - System.nanoTime();

            if (remaining <= 0) {
                return;
            }

            LockSupport.parkNanos(remaining);

            if (Thread.interrupted() && !running) {
                return;
            }
        }
    }
}
