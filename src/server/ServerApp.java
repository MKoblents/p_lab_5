package server;

import server.manager.CollectionManager;

import java.io.IOException;

public class ServerApp {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("=== Server Starting ===");

        try {
            CollectionManager collectionManager = new CollectionManager();
            collectionManager.loadFromFile(System.getenv("PLAB5"));
            System.out.println("Collection loaded. Size: " + collectionManager.size());
            Server server = new Server(PORT, collectionManager, System.getenv("PLAB5"));
            System.out.println("Server listening on port " + PORT);
            server.start();

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}