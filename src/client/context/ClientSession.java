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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientSession implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private final InputManager inputManager;
    private final ConnectionManager connection;
    private final ResponseHandler responseHandler;
    private final ScriptRunner scriptRunner;
    private final Invoker invoker;
    private final ClientContext context;
    private final SpawnClient spawnClientCommand;
    private final BlockingQueue<CommandResponse> responseQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;
    private AsyncNetworkReader networkReader;
    private Thread networkThread;
    private volatile boolean serverConnected = true;
    private volatile boolean processingForwardedCommand = false;
    private final Object inputLock = new Object();

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

        networkReader = new AsyncNetworkReader(
                connection.getSocketChannel(),
                () -> {
                    this.serverConnected = false;
                    this.running = false;
                    System.out.println("Server connection lost!");
                }
        );
        networkThread = new Thread(networkReader);
        networkThread.setDaemon(true);
        networkThread.start();
        Thread responseThread = new Thread(this::processResponsesLoop);
        responseThread.setDaemon(true);
        responseThread.start();
        while (running) {
            String commandKey;
            synchronized (inputLock) {
                while (processingForwardedCommand && running) {
                    try {
                        inputLock.wait(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                if (!running) break;
                commandKey = inputManager.parseCommand();
            }
            if (commandKey == null || commandKey.isEmpty()) continue;

            if ("execute_script".equals(commandKey)) {
                String path = inputManager.getLastPath();
                if (path != null) scriptRunner.executeScript(path);
                continue;
            }
            CommandRequest request = invoker.runCommand(commandKey);
            if (request != null) {
                connection.sendRequest(request);
            }
        }
    }

//    private void processNetworkMessages() {
//        CommandResponse response = networkReader.getResponseQueue().poll();
//        if (response != null) {
//            handleResponse(response);
//        }
//        CommandRequest forwarded = networkReader.getForwardQueue().poll();
//        if (forwarded != null) {
//            if (forwarded.args() instanceof ForwardCommandObject fco){
//                handleForwardedCommand(fco.commandKey());
//            }
//            else {
//                logger.warn("Not FCO Type");
//            }
//        }
//    }

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
        synchronized (inputLock) {
            processingForwardedCommand = true;
        }
        try {
            CommandRequest localRequest = invoker.runServerCommand(commandKey, SideFlag.FORWARDED);
            if (localRequest != null) {
                try {
                    connection.sendRequest(localRequest);
                    //                CommandResponse commandResponse =connection.readResponse();
                    //                if (commandResponse != null){
                    //                    handleResponse(commandResponse);
                    //                }
                } catch (IOException e) {
                    logger.error("Failed to send forwarded command request", e);
                    System.err.println("Network error while executing forwarded command");
                }
            } else {
                System.err.println("Failed to build request for forwarded command: " + commandKey);
            }
        }finally {
            synchronized (inputLock) {
                processingForwardedCommand = false;
                inputLock.notifyAll();
            }
        }
    }

    @Override
    public void close() {
        running = false;
        if (networkReader != null) networkReader.stop();
        connection.disconnect();
    }
    private void processResponsesLoop() {
        while (running) {
            CommandResponse response = networkReader.getResponseQueue().poll();
            if (response != null) {
                handleResponse(response);
            }
            CommandRequest forwarded = networkReader.getForwardQueue().poll();
            if (forwarded != null) {
                if (forwarded.args() instanceof ForwardCommandObject fco) {
                    handleForwardedCommand(fco.commandKey());
                } else {
                    logger.warn("Received unknown forwarded object type");
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}