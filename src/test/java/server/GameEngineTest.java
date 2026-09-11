package server;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameEngineTest {

    @Test
    void calculatesWaitFromCycleDuration() {
        assertWait(20, 580);
        assertWait(599, 1);
        assertWait(600, 0);
        assertWait(601, 599);
        assertWait(900, 300);
        assertWait(1199, 1);
        assertWait(1200, 600);
        assertWait(1201, 599);
    }

    private static void assertWait(long elapsedMillis, long expectedWaitMillis) {
        long elapsedNanos = TimeUnit.MILLISECONDS.toNanos(elapsedMillis);
        assertEquals(expectedWaitMillis, GameEngine.calculateWaitMillis(elapsedNanos));
    }
}
