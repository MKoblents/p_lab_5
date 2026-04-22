package client;

import client.command.Reconnect;
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
import client.scripts.FileManager;
import client.scripts.ScriptRunner;
import client.context.ClientSession;
import client.utils.RequestsFactory;
import shared.dto.CommandResponse;
import shared.dto.HandshakeRequest;
import shared.utils.LoggingConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
            boolean isRoot = (parentClientId == null);
            ClientContext context = new ClientContext(
                    clientId,
                    parentClientId,
                    connection,
                    isRoot
            );
            ResponseHandler responseHandler = new ResponseHandler(context);
            ClientProcessManager processManager = new ClientProcessManager(host, port);
            ScriptRunner scriptRunner = new ScriptRunner(
                    inputManager, connection, responseHandler, null, new FileManager()
            );
            logger.info("Successfully connected to server");
            System.out.println("Connected to server! Client ID: " + clientId);
            if (!isRoot) {
                System.out.println("This is a child client. Parent ID: " + parentClientId);
            } else {
                System.out.println("This is a ROOT client (direct connection to server)");
            }

            Invoker invoker = new Invoker(inputManager, context, connection, processManager, scriptRunner);
            ClientSession clientSession = new ClientSession(
                    inputManager, connection, responseHandler,
                    scriptRunner, invoker, context, processManager
            );
            invoker.registerCommand("reconnect", new Reconnect(connection,context,clientSession));
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
}