package client.context;

import client.command.SpawnClient;
import client.handlers.ResponseHandler;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import client.scripts.ScriptRunner;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.ForwardCommandObject;

import java.io.IOException;

public class ClientSession implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private final InputManager inputManager;
    private final ConnectionManager connection;
    private final ResponseHandler responseHandler;
    private final ScriptRunner scriptRunner;
    private final Invoker invoker;
    private final ClientContext context;
    private final SpawnClient spawnClientCommand;
    private final Object inputLock = new Object();
    private volatile boolean running = true;
    private AsyncNetworkReader networkReader;
    private Thread networkThread;

    public ClientSession(InputManager im, ConnectionManager conn, ResponseHandler rh,
                         ScriptRunner sr, Invoker invoker, ClientContext context,
                         ClientProcessManager processManager) {
        this.inputManager = im;
        this.connection = conn;
        this.responseHandler = rh;
        this.scriptRunner = sr;
        this.invoker = invoker;
        this.context = context;
        this.spawnClientCommand = new SpawnClient(context, connection, processManager);
    }

    public void run() throws IOException {
        System.out.println("Connected! Type 'help' for commands, 'exit' to quit.");

        networkReader = new AsyncNetworkReader(connection.getSocketChannel(), () -> {
            this.running = false;
            System.out.println("Server connection lost!");
        });
        networkThread = new Thread(networkReader);
        networkThread.setDaemon(true);
        networkThread.start();

        Thread responseThread = new Thread(this::processResponsesLoop);
        responseThread.setDaemon(true);
        responseThread.start();

        while (running) {
            if (!networkReader.getForwardQueue().isEmpty()) {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
                continue;
            }
            String commandKey = inputManager.parseCommand();
            if (commandKey != null && !commandKey.isEmpty()) {
                synchronized (inputLock) {
                    if ("execute_script".equals(commandKey)) {
                        String path = inputManager.getLastPath();
                        if (path != null) scriptRunner.executeScript(path);
                    } else {
                        CommandRequest request = invoker.runCommand(commandKey);
                        if (request != null) {
                            connection.sendRequest(request);
                        }
                    }
                }
            } else {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }
        }
    }

    private void processResponsesLoop() {
        while (running) {
            CommandResponse response = networkReader.getResponseQueue().poll();
            if (response != null) {
                handleResponse(response);
            }
            CommandRequest forwarded = networkReader.getForwardQueue().poll();
            if (forwarded != null) {
                synchronized (inputLock) {
                    if (forwarded.args() instanceof ForwardCommandObject fco) {
                        handleForwardedCommand(fco.commandKey());
                    } else {
                        logger.warn("Received unknown forwarded object type");
                    }
                }
            }
            try { Thread.sleep(50); } catch (InterruptedException e) { break; }
        }
    }

    private void handleResponse(CommandResponse response) {
        if ("spawn_client".equals(response.requestId()) ||
                (response.message() != null && response.message().contains("Child client created"))) {
            spawnClientCommand.handleResponse(response, context);
        } else {
            responseHandler.handle(response);
        }
    }

    private void handleForwardedCommand(String commandKey) {
        logger.info("Received forwarded command: {}", commandKey);
        System.out.println("\n[Received command from parent: " + commandKey + "]");
        CommandRequest localRequest = invoker.runServerCommand(commandKey, SideFlag.FORWARDED);
        if (localRequest != null) {
            try {
                connection.sendRequest(localRequest);
            } catch (IOException e) {
                logger.error("Failed to send forwarded command request", e);
                System.err.println("Network error while executing forwarded command");
            }
        } else {
            System.err.println("Failed to build request for forwarded command: " + commandKey);
        }
    }

    @Override
    public void close() {
        running = false;
        if (networkReader != null) networkReader.stop();
        connection.disconnect();
    }
}