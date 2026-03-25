package server;

import server.commands.*;
import server.manager.CollectionManager;
import server.manager.Invoker;
import server.outputWorkers.CollectionSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.SerializationUtil;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int DEFAULT_PORT = 12345;
    private static final String DEFAULT_DATA_FILE = "collection.xml";
    private static final String DEFAULT_LOG_LEVEL = "INFO";
    private static volatile boolean running = true;
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
            CollectionManager collectionManager = new CollectionManager();
            CollectionSaver collectionSaver = new CollectionSaver();
            logger.info("Loading collection from: {}", dataFile);
            collectionManager.loadFromFile(dataFile);
            logger.info("Loaded {} elements from {}",
                    collectionManager.getSpaceMarines().size(), dataFile);
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
            invoker.registerCommand("help", new HelpCommand(invoker));
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
                handleConsoleInput(collectionManager, collectionSaver, finalDataFile);
            });
            consoleThread.setDaemon(true);
            consoleThread.start();
            logger.info("Console reader started");
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                logger.debug("Server socket created and bound to port {}", port);
//                while (running) {
//                    logger.debug("Waiting for client connection...");
//                    Socket clientSocket = serverSocket.accept();
//                    logger.info("Client connected: {}", clientSocket.getRemoteSocketAddress());
//                    handleClient(clientSocket, invoker);
//                }
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket, invoker)).start();
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
    private static void handleConsoleInput(CollectionManager collectionManager,
                                           CollectionSaver collectionSaver,
                                           String dataFile) {
        Scanner scanner = new Scanner(System.in);
        logger.info("Console reader ready. Type 'save', 'exit', or 'help'.");
        while (running) {
            try {
                if (scanner.hasNextLine()) {
                    String command = scanner.nextLine().trim().toLowerCase();

                    switch (command) {
                        case "save":
                            logger.info("Manual save triggered from console");
                            try {
                                collectionSaver.save(collectionManager, dataFile);
                                logger.info("Collection saved to {}", dataFile);
                                System.out.println("✓ Collection saved successfully!");
                            } catch (Exception e) {
                                logger.error("Save failed: {}", e.getMessage(), e);
                                System.err.println("✗ Save failed: " + e.getMessage());
                            }
                            break;

                        case "exit":
                            logger.info("Exit command received from console");
                            System.out.println("Shutting down server...");
                            running = false;
                            System.exit(0);
                            break;
                        case "help":
                            System.out.println("Available server commands:");
                            System.out.println("  save - Save collection to file");
                            System.out.println("  exit - Stop server");
                            System.out.println("  help - Show this help");
                            break;
                        case "":
                            break;
                        default:
                            logger.warn("Unknown server command: {}", command);
                            System.err.println("Unknown command: " + command + ". Type 'help' for list.");
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error reading console: {}", e.getMessage());
            }
        }
    }
    private static void handleClient(Socket clientSocket, Invoker invoker) {
        try (
                java.io.InputStream in = clientSocket.getInputStream();
                java.io.OutputStream out = clientSocket.getOutputStream()
        ) {
            logger.debug("Streams opened for client: {}", clientSocket.getRemoteSocketAddress());
            while (!clientSocket.isClosed()) {
                try {
                    byte[] lengthBytes = new byte[4];
                    int bytesRead = 0;
                    while (bytesRead < 4) {
                        int read = in.read(lengthBytes, bytesRead, 4 - bytesRead);
                        if (read == -1) {
                            return;
                        }
                        bytesRead += read;
                    }
                    int length = java.nio.ByteBuffer.wrap(lengthBytes).getInt();
                    logger.trace("Request length: {} bytes", length);
                    byte[] data = new byte[length];
                    int dataRead = 0;
                    while (dataRead < length) {
                        int read = in.read(data, dataRead, length - dataRead);
                        if (read == -1) {
                            logger.warn("Unexpected disconnect while reading request");
                            return;
                        }
                        dataRead += read;
                    }
                    CommandRequest request = (CommandRequest) SerializationUtil.deserialize(data);
                    logger.debug("Received request: command={}, requestId={}",
                            request.commandType(), request.requestId());
                    CommandResponse response = invoker.runCommand(request);
                    logger.debug("Command executed: success={}", response.success());
                    byte[] responseData = SerializationUtil.serialize(response);
                    ByteBuffer responseBuffer = ByteBuffer.allocate(4 + responseData.length);
                    responseBuffer.putInt(responseData.length);
                    responseBuffer.put(responseData);
                    out.write(responseBuffer.array());
                    out.flush();
                    logger.info("Response sent for requestId: {}", response.requestId());
                } catch (java.io.EOFException e) {
                    logger.info("Client disconnected normally: {}", clientSocket.getRemoteSocketAddress());
                    break;
                } catch (IOException e) {
                    logger.error("IO error reading from client: {}", e.getMessage());
                    break;
                } catch (ClassNotFoundException e) {
                    logger.error("Deserialization error: {}", e.getMessage(), e);
                } catch (Exception e) {
                    logger.error("Unexpected error processing request: {}", e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            logger.error("IO error with client {}: {}",
                    clientSocket.getRemoteSocketAddress(), e.getMessage(), e);
        } finally {
            try {
                logger.debug("Closing connection to client: {}", clientSocket.getRemoteSocketAddress());
                clientSocket.close();
                logger.info("Client disconnected: {}", clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                logger.error("Error closing client socket: {}", e.getMessage());
            }
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