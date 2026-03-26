package client;

import client.config.ClientConfig;
import client.handlers.RequestBuilder;
import client.handlers.ResponseHandler;
import client.inputWorkers.CommandParser;
import client.inputWorkers.InputManager;
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
            RequestBuilder requestBuilder = new RequestBuilder(inputManager);
            ResponseHandler responseHandler = new ResponseHandler();
            ScriptRunner scriptRunner = new ScriptRunner(
                    inputManager, requestBuilder, connection, responseHandler
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
            System.out.println("Connected! Type 'help' for commands, 'exit' to quit.");
            ClientSession clientSession = new ClientSession(inputManager,requestBuilder,connection,responseHandler,scriptRunner);
            clientSession.run();
//            while (true) {
//                System.out.print("> ");
//                String commandKey = inputManager.parseCommand();
//                if (commandKey == null || commandKey.isEmpty()) {
//                    continue;
//                }
//                logger.debug("User entered command: '{}'", commandKey);
//                if (commandKey.equals("exit")) {
//                    logger.info("User requested exit");
//                    break;
//                }
//                if (commandKey.equals("execute_script")) {
//                    String path = inputManager.getLastPath();
//                    if (path == null) {
//                        logger.warn("execute_script called without path");
//                        System.err.println("Error: Script path required");
//                        continue;
//                    }
//                    logger.info("Executing script: {}", path);
//                    scriptRunner.executeScript(path);
//                    continue;
//                }
//                try {
//                    CommandRequest request = requestBuilder.buildRequest(commandKey);
//                    if (request == null) {
//                        logger.warn("Failed to build request for command: {}", commandKey);
//                        continue;
//                    }
//                    logger.debug("Sending request: {} with requestId: {}",
//                            request.commandType(), request.requestId());
//                    connection.sendRequest(request);
//                    logger.debug("Waiting for response...");
//                    CommandResponse response = connection.readResponse();
//                    if (response != null) {
//                        logger.debug("Received response for requestId: {}, success: {}",
//                                response.requestId(), response.success());
//                        responseHandler.handle(response);
//                    } else {
//                        logger.warn("No response received from server");
//                        System.err.println("No response from server");
//                    }
//                } catch (Exception e) {
//                    logger.error("Error processing command '{}': {}", commandKey, e.getMessage(), e);
//                    System.err.println("Error: " + e.getMessage());
//                }
//            }
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