package client;

import client.config.ClientConfig;
import client.context.ClientContext;
import client.handlers.ResponseHandler;
import client.hierarchy.PeerConnection;
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
    private static InputManager staticInputManager;
    private static Invoker staticInvoker;
    private static ClientContext staticContext;
    private static PeerConnection staticPeerConnection;
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
                System.err.println("Failed to connect. Exiting.");
                return;
            }
            String clientId = clientConfig.getClientId();
            String parentClientId = clientConfig.getParentClientId();
            try {
                HandshakeRequest handshake = new HandshakeRequest(clientId, parentClientId);
                connection.sendHandshake(handshake);
                logger.info("Handshake sent successfully. Client={}, Parent={}", clientId, parentClientId);
            } catch (IOException e) {
                logger.error("Failed to send handshake to server", e);
                System.err.println("Handshake failed: " + e.getMessage());
                connection.disconnect();
                return;
            }
            int parentPeerPort = clientConfig.getParentPeerPort();
            RequestsFactory.setClientId(clientId);
            logger.info("Using client ID: {}", clientId);
            staticConnection = connection;
            staticInputManager = inputManager;
            staticContext = null;
            staticPeerConnection = null;
            PeerConnection peerConnection = new PeerConnection();
            staticPeerConnection = peerConnection;
            int myPeerPort = peerConnection.startListening((message) -> {
               logger.debug("P2P message received in child: " + message);
                logger.debug("P2P message received: {}", message);
                if (message.startsWith("FORWARD:")) {
                    handleForwardCommand(message, peerConnection);
                }
                else if (message.startsWith("REGISTER_CHILD:")) {
                    handleChildRegistration(message);
                }
                else if (message.contains("PARENT_EXIT")) {
                    logger.info("Parent requested shutdown");
                    System.out.println("\n⚠ Parent client shut down, exiting...");
                    System.exit(0);
                }
                else if (message.startsWith("FORWARD_RESULT:")) {
                    logger.debug("Forward result received, handled by PeerConnection");
                }
            });
            boolean isRoot = (parentClientId == null);
            ClientContext context = new ClientContext(
                    clientId,
                    parentClientId,
                    connection,
                    isRoot,
                    myPeerPort
            );
            ResponseHandler responseHandler = new ResponseHandler(context);
            staticContext = context;
            if (parentClientId != null && parentPeerPort > 0) {
                try {
                   logger.debug("Registering with parent on port " + parentPeerPort);
                    peerConnection.sendToPeer(
                            "localhost",
                            parentPeerPort,
                            "REGISTER_CHILD:" + clientId + ":" + myPeerPort
                    );
                    logger.info("Registered with parent on port {}", parentPeerPort);
                    System.out.println("✓ Registered with parent client");
                } catch (IOException e) {
                    logger.warn("Failed to register with parent: {}", e.getMessage());
                    System.err.println("⚠ Could not register with parent: " + e.getMessage());
                }
            }
            ClientProcessManager processManager = new ClientProcessManager(host, port);
            Invoker invoker = new Invoker(inputManager, context, connection, processManager, peerConnection);
            staticInvoker = invoker;
            ScriptRunner scriptRunner = new ScriptRunner(
                    inputManager, connection, responseHandler, invoker
            );
            logger.info("Successfully connected to server");
            System.out.println("Connected to server! Client ID: " + clientId);
            if (!isRoot) {
                System.out.println("This is a child client. Parent ID: " + parentClientId);
            } else {
                System.out.println("This is a ROOT client (direct connection to server)");
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Client shutting down, notifying children...");
                for (String childId : context.getChildClientIds()) {
                    Integer childPort = context.getChildPeerPort(childId);
                    if (childPort != null) {
                        try {
                            peerConnection.sendToPeer("localhost", childPort, "PARENT_EXIT");
                            logger.info("Notified child {}", childId);
                        } catch (IOException e) {
                            logger.warn("Failed to notify child {}", childId);
                        }
                    }
                }
                processManager.destroyAllChildren();
            }));

            ClientSession clientSession = new ClientSession(
                    inputManager, connection, responseHandler,
                    scriptRunner, invoker, context, processManager
            );
            clientSession.run();

            logger.info("Disconnecting from server");
            connection.disconnect();
            logger.info("Client stopped");
            System.out.println("Client stopped.");
        } catch (Exception e) {
            logger.error("Fatal error in client", e);
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void handleForwardCommand(String message, PeerConnection peerConnection) {
        if (!processingForwardedCommand.compareAndSet(false, true)) {
            System.out.println("Another command is being processed, ignoring...");
            return;
        }
        String[] parts = message.split(":", 4);
        if (parts.length < 4) {
            logger.warn("Malformed FORWARD message: {}", message);
            processingForwardedCommand.set(false);
            return;
        }

        String fromClientId = parts[1];
        String requestId = parts[2];
        String command = parts[3];

        logger.info("Received forwarded command from {}: {}", fromClientId, command);
        System.out.println("\n Received command from client " + fromClientId + ": " + command);
       logger.debug("Starting to execute command...");

        synchronized (inputLock) {
            try {
               logger.debug("Temp reader set");
               logger.debug("Parsed command name: " + command);
               logger.debug("Calling runServerCommand...");
                CommandRequest request = staticInvoker.runServerCommand(command);
               logger.debug("runServerCommand returned: " + (request != null ? request.commandType() : "null"));

                CommandResponse response = null;
                String resultMsg;
                boolean success;

                if (request != null) {
                   logger.debug("Sending request to server...");
                    staticConnection.sendRequest(request);
                   logger.debug("Waiting for response...");
                    response = staticConnection.readResponse();
                    success = response != null && response.success();
                    resultMsg = response != null ? response.message() : "No response from server";
                   logger.debug("Response received - success=" + success);
                } else {
                    success = false;
                    resultMsg = "Failed to build request for command: " + command;
                   logger.debug("Request is null, command failed");
                }
                if (success) {
                    System.out.println("\n✓ Command '" + command + "' executed successfully");
                    if (response != null && response.result() != null) {
                        System.out.println("  Result: " + response.result());
                    }
                } else {
                    System.err.println("\nCommand '" + command + "' failed: " + resultMsg);
                }
               logger.debug("Original reader restored");

            } catch (Exception e) {
                System.err.println("✗ Failed to execute command: " + e.getMessage());
                e.printStackTrace();
            } finally {
                processingForwardedCommand.set(false);
            }
        }
    }
    private static void handleChildRegistration(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length >= 3) {
            String childId = parts[1];
            int childPort = Integer.parseInt(parts[2]);

            if (staticContext != null) {
                staticContext.registerChildPort(childId, childPort);
                staticContext.addChild(childId);
                logger.info("Child client {} registered with port {}", childId, childPort);
                System.out.println("New child client connected: " + childId);
            } else {
                logger.warn("Context not initialized, cannot register child: {}", childId);
            }
        } else {
            logger.warn("Malformed REGISTER_CHILD message: {}", message);
        }
    }
    private static Integer getClientPort(String clientId) {
        if (staticContext == null) {
            return null;
        }
        Integer port = staticContext.getChildPeerPort(clientId);
        if (port != null) {
            return port;
        }
        if (clientId.equals(staticContext.getParentClientId())) {
            return -1;
        }
        return null;
    }
    public static boolean isProcessingForwardedCommand() {
        return processingForwardedCommand.get();
    }
}
