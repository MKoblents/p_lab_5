package client;

import client.handlers.RequestBuilder;
import client.handlers.ResponseHandler;
import client.inputWorkers.CommandParser;
import client.inputWorkers.InputManager;
import client.io.ConsoleBufferedScanner;
import client.io.Reader;
import client.network.ConnectionManager;
import client.inputWorkers.XMLParser;
import client.scripts.ScriptRunner;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.io.IOException;

/**
 * Client application entry point and main loop.
 * Handles interactive input, builds requests, sends to server, displays responses.
 */
public class Client {

    private final InputManager inputManager;
    private final ConnectionManager connectionManager;
    private final RequestBuilder requestBuilder;
    private final ResponseHandler responseHandler;
    private final ScriptRunner scriptRunner;

    private boolean running = true;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        System.out.println("=== SpaceMarine Client ===");

        try {
            String host = DEFAULT_HOST;
            int port = DEFAULT_PORT;
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("--host") && i + 1 < args.length) {
                    host = args[++i];
                } else if (args[i].equals("--port") && i + 1 < args.length) {
                    port = Integer.parseInt(args[++i]);
                } else if (args[i].equals("--help")) {
                    printHelp();
                    return;
                }
            }
            Reader reader = new ConsoleBufferedScanner();
            CommandParser commandParser = new CommandParser();
            InputManager inputManager = new InputManager(reader, commandParser);
            ConnectionManager connectionManager = new ConnectionManager();
            RequestBuilder requestBuilder = new RequestBuilder(inputManager);
            ResponseHandler responseHandler = new ResponseHandler();
            XMLParser xmlParser = new XMLParser();
            ScriptRunner scriptRunner = new ScriptRunner(
                    inputManager,
                    requestBuilder,
                    connectionManager,
                    responseHandler,
                    xmlParser
            );
            Client client = new Client(
                    inputManager,
                    connectionManager,
                    requestBuilder,
                    responseHandler,
                    scriptRunner
            );
            client.start(host, port);
        } catch (IOException e) {
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            System.out.println("Client interrupted");
            Thread.currentThread().interrupt();
        }
    }

    public Client(InputManager inputManager,
                  ConnectionManager connectionManager,
                  RequestBuilder requestBuilder,
                  ResponseHandler responseHandler,
                  ScriptRunner scriptRunner) {
        this.inputManager = inputManager;
        this.connectionManager = connectionManager;
        this.requestBuilder = requestBuilder;
        this.responseHandler = responseHandler;
        this.scriptRunner = scriptRunner;
    }
    public void start(String host, int port) throws IOException, InterruptedException {
        System.out.println("Connecting to " + host + ":" + port + "...");
        if (!connectionManager.connect(host, port)) {
            System.err.println("Failed to connect. Exiting.");
            return;
        }
        System.out.println("Connected! Type 'help' for commands, 'exit' to quit.");
        while (running) {
            try {
                processUserInput();
            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
                handleConnectionError(host, port);
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
//            TODO save file
            return;
        }
        if (commandName.equals("execute_script")) {
            String scriptPath = inputManager.getLastPath();
            if (scriptPath == null || scriptPath.isEmpty()) {
                System.err.println("Error: Script path required");
                return;
            }
            scriptRunner.executeScript(scriptPath);
            return;
        }
        CommandRequest request = requestBuilder.buildRequest(commandName);
        if (request == null) {
            return;
        }
        connectionManager.sendRequest(request);
        CommandResponse response = connectionManager.waitForResponse(10000);
        if (response != null) {
            responseHandler.handle(response);
        } else {
            System.err.println("No response from server (timeout or connection lost)");
        }
    }

    private void handleConnectionError(String host, int port) {
        System.err.println("Attempting to reconnect...");
        if (!connectionManager.reconnect(host, port)) {
            System.err.println("Reconnection failed. Exiting.");
            running = false;
        } else {
            System.out.println("Reconnected successfully!");
        }
    }

    private static void printHelp() {
        //TODO normal help
        System.out.println("""
            SpaceMarine Client Usage:
              java -cp <classpath> client.Client [options]
            
            Options:
              --host <hostname>  Server hostname (default: localhost)
              --port <port>      Server port (default: 12345)
              --help             Show this help
            
            Commands (sent to server):
              help                       : show available commands
              info                       : show collection info
              add {element}              : add new SpaceMarine (XML)
              remove_by_id id            : remove element by ID
              update id {element}        : update element by ID
              clear                      : clear collection
              show                       : show all elements (sorted by name)
              sum_of_health              : sum of health values
              min_by_melee_weapon        : find min by melee weapon
              filter_less_than_melee_weapon type : filter by weapon
            
            Local commands:
              execute_script file_name   : execute script from local file
              exit                       : close client application
            """);
    }
}