package client.session;
import client.handlers.ResponseHandler;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.network.ConnectionManager;
import client.scripts.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.io.IOException;

public class ClientSession implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private final InputManager inputManager;
    private final ConnectionManager connection;
    private final ResponseHandler responseHandler;
    private final ScriptRunner scriptRunner;
    private Invoker invoker;

    public ClientSession(InputManager im,
                         ConnectionManager conn, ResponseHandler rh,
                         ScriptRunner sr, Invoker invoker) {
        this.inputManager = im;
        this.connection = conn;
        this.responseHandler = rh;
        this.scriptRunner = sr;
        this.invoker = invoker;
    }

    public void run() throws IOException {
        System.out.println("Connected! Type 'help' for commands, 'exit' to quit.");
        while (true) {
            System.out.print("> ");
            String commandKey = inputManager.parseCommand();
            if (commandKey == null || commandKey.isEmpty()) {
                continue;
            }
            logger.debug("User entered command: '{}'", commandKey);
//            if (commandKey.equals("exit")) {
//                logger.info("User requested exit");
//                break;
//            }
            if (commandKey.equals("execute_script")) {
                String path = inputManager.getLastPath();
                if (path == null) {
                    logger.warn("execute_script called without path");
                    System.err.println("Error: Script path required");
                    continue;
                }
                logger.info("Executing script: {}", path);
                scriptRunner.executeScript(path);
                continue;
            }
            try {
                CommandRequest request = invoker.runCommand(commandKey);
                if (request == null) {
                    logger.warn("Failed to build request for command: {}", commandKey);
                    continue;
                }
                logger.debug("Sending request: {} with requestId: {}",
                        request.commandType(), request.requestId());
                connection.sendRequest(request);
                logger.debug("Waiting for response...");
                CommandResponse response = connection.readResponse();
                if (response != null) {
                    logger.debug("Received response for requestId: {}, success: {}",
                            response.requestId(), response.success());
                    responseHandler.handle(response);
                } else {
                    logger.warn("No response received from server");
                    System.err.println("No response from server");
                }
            } catch (Exception e) {
                logger.error("Error processing command '{}': {}", commandKey, e.getMessage(), e);
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    @Override
    public void close(){
        logger.info("Disconnecting from server");
        try {
            connection.disconnect();
        } catch (Exception e) {
            logger.error("Error during disconnect: {}", e.getMessage());
        }
    }
}
