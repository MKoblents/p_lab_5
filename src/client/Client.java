package client;

import client.handlers.RequestBuilder;
import client.handlers.ResponseHandler;
import client.inputWorkers.CommandParser;
import client.inputWorkers.InputManager;
import client.io.ConsoleBufferedScanner;
import client.io.Reader;
import client.network.ConnectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.io.IOException;

public class Client {
    private final InputManager inputManager;
    private final ConnectionManager connectionManager;
    private final RequestBuilder requestBuilder;
    private final ResponseHandler responseHandler;

    private boolean running = true;

    public Client() throws IOException {
        Reader reader = new ConsoleBufferedScanner();
        CommandParser parser = new CommandParser();
        this.inputManager = new InputManager(reader, parser);
        this.connectionManager = new ConnectionManager();
        this.requestBuilder = new RequestBuilder(inputManager);
        this.responseHandler = new ResponseHandler();
    }

    public void start() {
        System.out.println("Connecting to server...");

        if (!connectionManager.connect("localhost", 12345)) {
            System.err.println("Failed to connect to server");
            return;
        }

        System.out.println("Connected! Type 'help' for commands, 'exit' to quit.");
        while (running) {
            try {
                processUserInput();
            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
                break;
            }
        }

        connectionManager.disconnect();
        System.out.println("Client stopped.");
    }

    private void processUserInput() throws IOException {
        System.out.print("> ");
        String commandName = inputManager.parseCommand();
        if (commandName == null || commandName.isEmpty()) {
            return;
        }
        if (commandName.equals("exit")) {
            running = false;
            return;
        }
        CommandRequest request = requestBuilder.buildRequest(commandName);
        if (request == null) {
            System.err.println("Failed to build request for command: " + commandName);
            return;
        }
        connectionManager.sendRequest(request);
        CommandResponse response = connectionManager.waitForResponse(5000); // 5s timeout
        if (response != null) {
            responseHandler.handle(response);
        } else {
            System.err.println("No response from server (timeout)");
        }
    }

}