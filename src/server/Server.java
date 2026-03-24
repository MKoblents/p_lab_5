package server;

import server.commands.*;
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
 * Single-threaded NIO server with graceful shutdown and extended logging.
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_DATA_FILE = "collection.xml";
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String dataFile = System.getenv("PLAB5") != null
                ? System.getenv("PLAB5")
                : DEFAULT_DATA_FILE;
        String logLevel = DEFAULT_LOG_LEVEL;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") && i + 1 < args.length) {
                port = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--file") && i + 1 < args.length) {
                dataFile = args[++i];
            } else if (args[i].equals("--log-level") && i + 1 < args.length) {
                logLevel = args[++i].toUpperCase();
            }
        }
        setLogLevel(logLevel);
        logger.info("Log level set to: {}", logLevel);
        logger.info("=== SpaceMarine Server Starting ===");
        logger.info("Configuration: port={}, dataFile={}", port, dataFile);
        try {
            FileManager fileManager = new FileManager();
            CollectionManager collectionManager = new CollectionManager();
            CollectionSaver collectionSaver = new CollectionSaver();
            logger.info("Loading collection from: {}", dataFile);
            collectionManager.loadFromFile(dataFile);
            logger.info("Loaded {} elements from {}",
                    collectionManager.getSpaceMarines().size(), dataFile);
            logger.debug("Opening server socket channel on port {}", port);
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));
            logger.debug("Server socket bound to port {}", port);
            Selector selector = Selector.open();
            serverChannel.register(selector, java.nio.channels.SelectionKey.OP_ACCEPT);
            logger.debug("Server channel registered with selector for OP_ACCEPT");
            Invoker invoker = new Invoker();
            logger.debug("Registering commands with Invoker");
            invoker.registerCommand("add", new AddCommand(collectionManager));
            invoker.registerCommand("clear", new ClearCommand(collectionManager));
            invoker.registerCommand("filter_less_than_melee_weapon", new FilterLessThanMeleeWeaponCommand(collectionManager));
            invoker.registerCommand("info", new InfoCommand(collectionManager));
            invoker.registerCommand("insert_at", new InsertAtCommand(collectionManager));
            invoker.registerCommand("min_by_melee_weapon", new MinByMeleeWeaponCommand(collectionManager));
            invoker.registerCommand("remove_by_id", new RemoveByIdCommand(collectionManager));
            invoker.registerCommand("remove_greater", new RemoveGreaterCommand(collectionManager));
            invoker.registerCommand("show", new ShowCommand(collectionManager));
            invoker.registerCommand("shuffle", new ShuffleCommand(collectionManager));
            invoker.registerCommand("sum_of_health", new SumOfHealthCommand(collectionManager));
            invoker.registerCommand("update", new UpdateCommand(collectionManager));
            ServerConnectionHandler connectionHandler = new ServerConnectionHandler(selector, invoker);
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
            System.out.println("Server running. Press Ctrl+C to stop.");
            logger.debug("Entering main event loop");
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                for (var key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        logger.trace("Processing OP_ACCEPT event");
                        connectionHandler.handleAccept(key);
                    } else if (key.isReadable()) {
                        logger.trace("Processing OP_READ event");
                        connectionHandler.handleRead(key);
                    }
                }
                selector.selectedKeys().clear();
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
    private static void setLogLevel(String level) {
        try {
            ch.qos.logback.classic.Logger root =
                    (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(ch.qos.logback.classic.Level.toLevel(level));
            logger.debug("Logback root level set to: {}", level);
        } catch (Exception e) {
            System.err.println("Warning: Could not set log level to " + level + ", using default");
            logger.warn("Could not set log level to {}, using default", level);
        }
    }
}