package client;

import client.config.ClientConfig;
import client.handlers.ResponseHandler;
import client.inputWorkers.CommandParser;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.io.ConsoleBufferedScanner;
import client.io.Reader;
import client.network.ConnectionManager;
import client.scripts.ScriptRunner;
import client.session.ClientSession;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.utils.LoggingConfigurator;
import shared.utils.XMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

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
            Invoker invoker = new Invoker(inputManager);
            ScriptRunner scriptRunner = new ScriptRunner(
                    inputManager, connection, responseHandler, invoker
            );

            logger.info("Connecting to {}:{}", host, port);
            System.out.println("=== SpaceMarine Client ===");
            System.out.println("Connecting to " + host + ":" + port + "...");
            if (!connection.connect(host, port)) {
                logger.error("Failed to connect to server at {}:{}", host, port);
                System.err.println("Failed to connect. Exiting.");
                return;
            }
            logger.info("Successfully connected to server");
            ClientSession clientSession = new ClientSession(inputManager,connection,responseHandler,scriptRunner, invoker);
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