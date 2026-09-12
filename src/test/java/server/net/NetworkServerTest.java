package server.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkServerTest {

    @Test
    void startsAndStops() throws InterruptedException {
        NetworkServer server = new NetworkServer(0);

        try {
            server.start();
            assertTrue(server.isRunning());
        } finally {
            server.stop();
        }

        assertFalse(server.isRunning());
    }
}
