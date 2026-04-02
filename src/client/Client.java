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
import shared.utils.LoggingConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

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
            ResponseHandler responseHandler = new ResponseHandler();



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
            int parentPeerPort = clientConfig.getParentPeerPort();
            RequestsFactory.setClientId(clientId);
            logger.info("Using client ID: {}", clientId);
            PeerConnection peerConnection = new PeerConnection();
            int myPeerPort = peerConnection.startListening((message) -> {
                if (message.contains("PARENT_EXIT")) {
                    logger.info("Parent requested shutdown");
                    System.exit(0);
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

            if (parentClientId != null && parentPeerPort > 0) {
                try {
                    peerConnection.sendToPeer(
                            "localhost",
                            parentPeerPort,
                            "REGISTER_CHILD:" + clientId
                    );
                    logger.info("Registered with parent on port {}", parentPeerPort);
                } catch (IOException e) {
                    logger.warn("Failed to register with parent: {}", e.getMessage());
                }
            }
            ClientProcessManager processManager = new ClientProcessManager(host, port);
            Invoker invoker = new Invoker(inputManager,context,connection,processManager, peerConnection);
            ScriptRunner scriptRunner = new ScriptRunner(
                    inputManager, connection, responseHandler, invoker
            );
            logger.info("Successfully connected to server");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Client shutting down, notifying children...");
                for (String childId : context.getChildClientIds()) {
                    Integer peerPort = context.getChildPeerPort(childId);
                    if (peerPort != null) {
                        try {
                            peerConnection.sendToPeer("localhost", peerPort, "PARENT_EXIT");
                        } catch (IOException e) {
                            logger.warn("Failed to notify child {}", childId);
                        }
                    }
                }
                processManager.destroyAllChildren();
            }));
            ClientSession clientSession = new ClientSession(inputManager,connection,responseHandler,scriptRunner, invoker, context,processManager);
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



}