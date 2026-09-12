package server;

import server.net.NetworkServer;
import server.world.World;

public final class Server {

    private final World world = new World();
    private final GameEngine gameEngine = new GameEngine(world);
    private final NetworkServer networkServer = new NetworkServer(world);

    public static void main(String[] args) throws InterruptedException {
        new Server().start();
    }

    private void start() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Shutdown"));

        gameEngine.start();
        try {
            networkServer.start();
        } catch (InterruptedException | RuntimeException exception) {
            gameEngine.stop();
            throw exception;
        }

        System.out.println("317 Server started.");
    }

    private void stop() {
        networkServer.stop();
        gameEngine.stop();
    }
}
