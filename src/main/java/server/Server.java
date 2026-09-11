package server;

import server.world.World;

public final class Server {

    private final World world = new World();
    private final GameEngine gameEngine = new GameEngine(world);

    public static void main(String[] args) {
        new Server().start();
    }

    private void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(gameEngine::stop, "Shutdown"));
        gameEngine.start();

        System.out.println("317 Server started.");
    }
}
