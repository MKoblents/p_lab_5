package client;

import client.config.ClientConfig;
import client.context.ClientContext;
import client.handlers.ResponseHandler;
import client.inputWorkers.CommandParser;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.io.ConsoleBufferedScanner;
import client.io.Reader;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import client.scripts.ScriptRunner;
import client.context.ClientSession;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
import shared.models.SpaceMarine;
import shared.utils.LoggingConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static ConnectionManager staticConnection;
    private static Invoker staticInvoker;
    private static ClientContext staticContext;
    private static final AtomicBoolean processingForwardedCommand = new AtomicBoolean(false);
    public static final Object inputLock = new Object();

    public static void main(String[] args) {
        ClientConfig clientConfig = ClientConfig.parse(args);
        String host = clientConfig.getHost();
        int port = clientConfig.getPort();
        String logLevel = clientConfig.getLogLevel();
        LoggingConfigurator.configure(logLevel);
        logger.info("Client starting with log level: {}", logLevel);

        try {
            Reader reader = new ConsoleBufferedScanner();
            CommandParser parser = new CommandParser();
            InputManager inputManager = new InputManager(reader, parser);
            ConnectionManager connection = new ConnectionManager();


            logger.info("Connecting to {}:{}", host, port);
            System.out.println("=== SpaceMarine Client ===");
            System.out.println("Connecting to " + host + ":" + port + "...");
            if (!connection.connect(host, port)) {
                logger.error("Failed to connect to server at {}:{}", host, port);
                System.err.println("Failed to connect.");
//                return;
            }
            String clientId = clientConfig.getClientId();
            String parentClientId = clientConfig.getParentClientId();
            RequestsFactory.setClientId(clientId);
            logger.info("Using client ID: {}", clientId);
            staticConnection = connection;
            staticContext = null;
            boolean isRoot = (parentClientId == null);
            ClientContext context = new ClientContext(
                    clientId,
                    parentClientId,
                    connection,
                    isRoot
            );
            ResponseHandler responseHandler = new ResponseHandler(context);
            staticContext = context;
            ClientProcessManager processManager = new ClientProcessManager(host, port);
            ScriptRunner scriptRunner = new ScriptRunner(
                    inputManager, connection, responseHandler, null
            );
            Invoker invoker = new Invoker(inputManager, context, connection, processManager, scriptRunner);
            logger.info("Successfully connected to server");
            System.out.println("Connected to server! Client ID: " + clientId);
            if (!isRoot) {
                System.out.println("This is a child client. Parent ID: " + parentClientId);
            } else {
                System.out.println("This is a ROOT client (direct connection to server)");
            }

            ClientSession clientSession = new ClientSession(
                    inputManager, connection, responseHandler,
                    scriptRunner, invoker, context, processManager
            );
//            try {
////                HandshakeRequest handshake = new HandshakeRequest(clientId, parentClientId);
////                connection.sendHandshake(handshake);
////                logger.info("Handshake sent successfully. Client={}, Parent={}", clientId, parentClientId);
////                CommandResponse handshakeResponse = connection.readResponse();
//            } catch (IOException e) {
//                logger.error("Failed to send handshake to server", e);
//                System.err.println("Handshake failed: " + e.getMessage());
////                connection.disconnect();
//
//                return;
//            }
            try {
                if (connection.isConnected()) {
                    HandshakeRequest handshake = new HandshakeRequest(clientId, parentClientId);
                    connection.sendHandshake(handshake);
                    logger.info("Handshake sent successfully. Client={}, Parent={}", clientId, parentClientId);
                    CommandResponse handshakeResponse = connection.readResponse();

                    clientSession.run();
                } else {
                    clientSession.runOffline();
                }
            } catch (IOException e) {
                logger.info("Client session ended: {}", e.getMessage());
            } finally {
                clientSession.close();
            }
            logger.info("Client stopped");
            System.out.println("Client stopped.");
        } catch (Exception e) {
            logger.error("Fatal error in client", e);
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
//    private static void handleForwardCommand(String message) {
//        if (!processingForwardedCommand.compareAndSet(false, true)) {
//            System.out.println("Another command is being processed, ignoring...");
//            return;
//        }
//        String[] parts = message.split(":", 4);
//        if (parts.length < 4) {
//            logger.warn("Malformed FORWARD message: {}", message);
//            processingForwardedCommand.set(false);
//            return;
//        }
//
//        String fromClientId = parts[1];
//        String requestId = parts[2];
//        String command = parts[3];
//
//        logger.info("Received forwarded command from {}: {}", fromClientId, command);
//        System.out.println("\n Received command from client " + fromClientId + ": " + command);
//       logger.debug("Starting to execute command...");
//
//        synchronized (inputLock) {
//            try {
//               logger.debug("Temp reader set");
//               logger.debug("Parsed command name: " + command);
//               logger.debug("Calling runServerCommand...");
//                CommandRequest request = staticInvoker.runServerCommand(command);
//               logger.debug("runServerCommand returned: " + (request != null ? request.commandType() : "null"));
//
//                CommandResponse response = null;
//                String resultMsg;
//                boolean success;
//
//                if (request != null) {
//                   logger.debug("Sending request to server...");
//                    staticConnection.sendRequest(request);
//                   logger.debug("Waiting for response...");
//                    response = staticConnection.readResponse();
//                    success = response != null && response.success();
//                    resultMsg = response != null ? response.message() : "No response from server";
//                   logger.debug("Response received - success=" + success);
//                } else {
//                    success = false;
//                    resultMsg = "Failed to build request for command: " + command;
//                   logger.debug("Request is null, command failed");
//                }
//                if (success) {
//                    System.out.println("\n✓ Command '" + command + "' executed successfully");
//                    if (response != null && response.result() != null) {
//                        System.out.println("  Result: " + response.result());
//                    }
//                } else {
//                    System.err.println("\nCommand '" + command + "' failed: " + resultMsg);
//                }
//               logger.debug("Original reader restored");
//
//            } catch (Exception e) {
//                System.err.println("✗ Failed to execute command: " + e.getMessage());
//                e.printStackTrace();
//            } finally {
//                processingForwardedCommand.set(false);
//            }
//        }
//    }
//    private static void handleChildRegistration(String message) {
//        String[] parts = message.split(":", 3);
//        if (parts.length >= 3) {
//            String childId = parts[1];
//            int childPort = Integer.parseInt(parts[2]);
//
//            if (staticContext != null) {
//                staticContext.addChild(childId);
//                logger.info("Child client {} registered with port {}", childId, childPort);
//                System.out.println("New child client connected: " + childId);
//            } else {
//                logger.warn("Context not initialized, cannot register child: {}", childId);
//            }
//        } else {
//            logger.warn("Malformed REGISTER_CHILD message: {}", message);
//        }
//    }
//    public static boolean isProcessingForwardedCommand() {
//        return processingForwardedCommand.get();
//    }
}
