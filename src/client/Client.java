package client;

import client.handlers.RequestBuilder;
import client.handlers.ResponseHandler;
import client.inputWorkers.CommandParser;
import client.inputWorkers.InputManager;
import client.io.ConsoleBufferedScanner;
import client.io.Reader;
import client.network.ConnectionManager;
import shared.utils.XMLParser;
import client.scripts.ScriptRunner;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        System.out.println("=== SpaceMarine Client ===");
        String host = "localhost";
        int port = 12345;
        try {
            Reader reader = new ConsoleBufferedScanner();
            CommandParser parser = new CommandParser();
            InputManager inputManager = new InputManager(reader, parser);
            ConnectionManager connection = new ConnectionManager();
            RequestBuilder requestBuilder = new RequestBuilder(inputManager);
            ResponseHandler responseHandler = new ResponseHandler();
            XMLParser xmlParser = new XMLParser();
            ScriptRunner scriptRunner = new ScriptRunner(inputManager, requestBuilder, connection, responseHandler
            );
            System.out.println("Connecting to " + host + ":" + port + "...");
            if (!connection.connect(host, port)) {
                System.err.println("Failed to connect. Exiting.");
                return;
            }
            System.out.println("Connected! Type 'help' for commands, 'exit' to quit.");
            ConsoleBufferedScanner scanner = new ConsoleBufferedScanner();
            while (true) {
                System.out.print("> ");
                String commandKey = inputManager.parseCommand();
                if (commandKey == null || commandKey.isEmpty()) {
                    continue;
                }
                if (commandKey.equals("exit")) {
                    break;
                }
                if (commandKey.equals("execute_script")) {
                    String path = inputManager.getLastPath();
                    if (path == null) {
                        System.err.println("Error: Script path required");
                        continue;
                    }
                    scriptRunner.executeScript(path);
                    continue;
                }
                try {
                    CommandRequest request = requestBuilder.buildRequest(commandKey);
                    if (request == null) {
                        continue;
                    }
                    connection.sendRequest(request);
                    CommandResponse response = connection.readResponse();
                    if (response != null) {
                        responseHandler.handle(response);
                    } else {
                        System.err.println("No response from server");
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
            connection.disconnect();
            System.out.println("Client stopped.");

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}