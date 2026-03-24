package server;

import server.manager.CollectionManager;
import server.manager.FileManager;
import server.manager.Invoker;
import server.network.ServerConnectionHandler;
import server.outputWorkers.CollectionSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.Selector;

/**
 * Server application entry point.
 * Single-threaded NIO server with graceful shutdown.
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_DATA_FILE = "collection.xml";

    public static void main(String[] args) {
        logger.info("=== SpaceMarine Server ===");

        try {
            // Parse arguments: --port <p> --file <path>
            int port = DEFAULT_PORT;
            String dataFile = System.getenv("PLAB5") != null
                    ? System.getenv("PLAB5")
                    : DEFAULT_DATA_FILE;

            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--port") && i + 1 < args.length) {
                    port = Integer.parseInt(args[++i]);
                } else if (args[i].equals("--file") && i + 1 < args.length) {
                    dataFile = args[++i];
                }
            }

            // Initialize managers
            FileManager fileManager = new FileManager(dataFile);
            CollectionManager collectionManager = new CollectionManager(fileManager);
            CollectionSaver collectionSaver = new CollectionSaver();

            // Load collection at startup
            collectionManager.loadFromFile();
            logger.info("Loaded {} elements from {}",
                    collectionManager.getSpaceMarines().size(), dataFile);

            // Setup NIO
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);  // NON-BLOCKING!
            serverChannel.bind(new InetSocketAddress(port));

            Selector selector = Selector.open();
            serverChannel.register(selector, java.nio.channels.SelectionKey.OP_ACCEPT);

            // Initialize command invoker (registers all commands including help)
            Invoker invoker = new Invoker(collectionManager, fileManager, collectionSaver);

            // Initialize connection handler
            ServerConnectionHandler connectionHandler =
                    new ServerConnectionHandler(selector, invoker, collectionManager);

            // Register shutdown hook for auto-save
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook: saving collection...");
                try {
                    collectionSaver.save(collectionManager, dataFile);
                    logger.info("Collection saved to {}", dataFile);
                } catch (Exception e) {
                    logger.error("Error saving on shutdown: {}", e.getMessage(), e);
                }
            }));

            logger.info("Server started on port {}", port);
            System.out.println("Server running. Press Ctrl+C to stop.");

            // Main event loop (SINGLE-THREADED)
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();  // Blocks until event occurs

                for (var key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        connectionHandler.handleAccept(key);
                    } else if (key.isReadable()) {
                        connectionHandler.handleRead(key);
                    }
                }
                selector.selectedKeys().clear();
            }

        } catch (IOException e) {
            logger.error("Server error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}