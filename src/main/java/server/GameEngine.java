package server;

import server.world.World;

import java.util.concurrent.locks.LockSupport;

public final class GameEngine implements Runnable {

    public static final long CYCLE_LENGTH_MILLIS = 600L;
    private static final long CYCLE_LENGTH_NANOS = CYCLE_LENGTH_MILLIS * 1_000_000L;

    private final World world;

    private volatile boolean running;
    private Thread thread;

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
        long nextCycle = System.nanoTime();

        while (running) {
            waitUntil(nextCycle);

            if (!running) {
                break;
            }

            long cycleStart = System.nanoTime();
            world.cycle();
            long cycleEnd = System.nanoTime();
            long cycleTime = cycleEnd - cycleStart;

            if (cycleTime > CYCLE_LENGTH_NANOS) {
                System.err.printf(
                        "Cycle %d took %.2fms%n",
                        world.getCycle(),
                        cycleTime / 1_000_000.0
                );
            }

            nextCycle += CYCLE_LENGTH_NANOS;

            if (cycleEnd > nextCycle) {
                nextCycle = cycleEnd;
            }
        }
    }

    private void waitUntil(long targetTime) {
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
