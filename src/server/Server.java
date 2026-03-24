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
            FileManager fileManager = new FileManager();
            CollectionManager collectionManager = new CollectionManager();
            CollectionSaver collectionSaver = new CollectionSaver();
            collectionManager.loadFromFile(dataFile);
            logger.info("Loaded {} elements from {}",
                    collectionManager.getSpaceMarines().size(), dataFile);
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));

            Selector selector = Selector.open();
            serverChannel.register(selector, java.nio.channels.SelectionKey.OP_ACCEPT);
            Invoker invoker = new Invoker();
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
            ServerConnectionHandler connectionHandler =
                    new ServerConnectionHandler(selector, invoker);
            final String finalDataFile = dataFile;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook: saving collection...");
                try {
                    collectionSaver.save(collectionManager, finalDataFile);
                    logger.info("Collection saved to {}", finalDataFile);
                } catch (Exception e) {
                    logger.error("Error saving on shutdown: {}", e.getMessage(), e);
                }
            }));
            logger.info("Server started on port {}", port);
            System.out.println("Server running. Press Ctrl+C to stop.");
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();

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