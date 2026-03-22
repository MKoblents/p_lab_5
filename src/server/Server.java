package server;

import manager.CollectionManager;

import java.io.IOException;

public class Server {
    private final int port;
    private final CollectionManager collectionManager;
    private final String filePath;

    public Server(int port, CollectionManager collectionManager, String filePath) {
        this.port = port;
        this.collectionManager = collectionManager;
        this.filePath = filePath;
    }

    public void start() throws IOException {
        System.out.println("Server started (stub)");
    }
}