package server;

import server.manager.ClientRegistry;
import shared.utils.LoggingConfigurator;
import server.config.ServerConfig;
import server.console.ConsoleHandler;
import server.manager.CollectionManager;
import server.manager.Invoker;
import server.network.ClientHandler;
import server.outputWorkers.CollectionSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final AtomicBoolean running = new AtomicBoolean(true);
    public static void main(String[] args) {
        ServerConfig config = ServerConfig.parse(args);
        int port = config.getPort();
        String dataFile = config.getFile();
        String logLevel = config.getLogLevel();
        LoggingConfigurator.configure(logLevel);
        logger.info("Log level set to: {}", logLevel);
        logger.info("=== SpaceMarine Server Starting ===");
        logger.info("Configuration: port={}, dataFile={}", port, dataFile);
        try {
            CollectionManager collectionManager = new CollectionManager();
            CollectionSaver collectionSaver = new CollectionSaver();
            ClientRegistry clientRegistry = new ClientRegistry();
            logger.info("Loading collection from: {}", dataFile);
            collectionManager.loadFromFile(dataFile);
            logger.info("Loaded {} elements from {}",
                    collectionManager.getSpaceMarines().size(), dataFile);
            Invoker invoker = new Invoker(collectionManager);
            final String finalDataFile = dataFile;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("=== Shutdown hook triggered ===");
                logger.info("Saving collection before shutdown...");
                try {
                    collectionSaver.save(collectionManager, finalDataFile);
                    logger.info("Collection saved successfully to {}", finalDataFile);
                } catch (Exception e) {
                    logger.error("Error saving collection on shutdown: {}", e.getMessage(), e);
                }
                logger.info("=== Server shutdown complete ===");
            }));
            logger.info("=== SpaceMarine Server Started ===");
            logger.info("Server listening on port {}", port);
            System.out.println("Server running. Press Ctrl+C to stop or type 'save'/'exit'.");
            Thread consoleThread = new Thread(() -> {
                new ConsoleHandler(collectionManager, collectionSaver, finalDataFile, running).handleConsoleInput();
            });
            consoleThread.setDaemon(true);
            consoleThread.start();
            logger.info("Console reader started");
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                logger.debug("Server socket created and bound to port {}", port);
                while (running.get()) {
                    logger.debug("Waiting for client connection...");
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Client connected: {}", clientSocket.getRemoteSocketAddress());
                    new Thread(() -> ClientHandler.handleClient(clientSocket, invoker, clientRegistry)).start();
                }
            }
            logger.info("Server main loop exited");
        } catch (IOException e) {
            logger.error("Fatal IO error in server: {}", e.getMessage(), e);
            System.err.println("Server error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error in server: {}", e.getMessage(), e);
            System.err.println("Unexpected error: " + e.getMessage());
            System.exit(1);
        }
    }

}