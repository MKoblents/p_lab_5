package client.context;

import client.command.SpawnClient;
import client.handlers.ResponseHandler;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import client.scripts.ScriptRunner;
import client.utils.DisconnectReason;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.ForwardCommandObject;
import shared.dto.HandshakeRequest;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientSession implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private volatile ScheduledExecutorService heartbeatScheduler;
    private static final int HEARTBEAT_INTERVAL_SEC = 5;
    private static final String CMD_HEARTBEAT = "heartbeat";
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
    private volatile Thread mainThread;

    public ClientSession(InputManager im, ConnectionManager conn, ResponseHandler rh,
                         ScriptRunner sr, Invoker invoker, ClientContext context,
                         ClientProcessManager processManager) {
        this.inputManager = im;
        this.connection = conn;
        this.responseHandler = rh;
        this.scriptRunner = sr;
        scriptRunner.setInvoker(invoker);
        this.invoker = invoker;
        this.context = context;
        this.spawnClientCommand = new SpawnClient(context, connection, processManager);
    }

    public void run() throws IOException {
        System.out.println("Connected! Type 'help' for commands, 'exit' to quit.");
        mainThread = Thread.currentThread();
        networkReader = new AsyncNetworkReader(connection.getSocketChannel(), reason -> {
            System.out.println("  Connection lost: " + reason);
            connection.setConnected(false);
            if (reason == DisconnectReason.PARENT_DOWN) {
                System.out.println("Parent exit. Exiting...");
                running = false;
                if (mainThread != null) mainThread.interrupt();
            }
           });
        networkThread = new Thread(networkReader);
        networkThread.setDaemon(true);
        networkThread.start();

        Thread responseThread = new Thread(this::processResponsesLoop);
        responseThread.setDaemon(true);
        responseThread.start();
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heatbeat-scheduler");
            t.setDaemon(true);
            return t;
        });
        Runnable heartbeatTask = ()->{
            try {
                CommandRequest request = new CommandRequest(
                        CommandRequest.CMD_HEARTBEAT,
                        null,
                        UUID.randomUUID().toString().substring(0,8),
                        context.getClientId());
                connection.sendRequest(request);
            } catch (IOException | RuntimeException e) {
                logger.warn("Heartbeat failed: {}", e.getMessage());
            }
        };
        heartbeatScheduler.scheduleWithFixedDelay(heartbeatTask,0, 5, TimeUnit.SECONDS);

        while (running) {
            if (!networkReader.getForwardQueue().isEmpty()) {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
                continue;
            }
            String commandKey = inputManager.parseCommand();
            if (commandKey != null && !commandKey.isEmpty()) {
                synchronized (inputLock) {
                    CommandRequest request = invoker.runCommand(commandKey);
                    if (request != null) {
                        try {
                            if (!connection.isConnected()){
                                System.out.println("Not connected to server. Can't send request.");
                            }
                            else {
                                connection.sendRequest(request);
                            }
                        }catch (IOException e){
                            System.err.println("Network error while sending request: " + e.getMessage());

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
                if ("PARENT_TERMINATED".equals(response.message())) {
                    System.out.println("Parent exited. Exiting...");
                    running = false;
                    connection.setConnected(false);
                    if (mainThread != null) mainThread.interrupt();
                    continue;
                }
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
        if (mainThread != null) mainThread.interrupt();
        if (networkReader != null) networkReader.stop();
        connection.disconnect();
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdownNow();
            try {
                heartbeatScheduler.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    public void runOffline() throws IOException {
        System.out.println("\n OFFLINE MODE: No server connection.");
        System.out.println("   Available commands: help, exit, reconnect");
        mainThread = Thread.currentThread();
        while (running) {
            String commandKey = inputManager.parseCommand();
            if (commandKey != null && !commandKey.isEmpty()) {
                if ("reconnect".equalsIgnoreCase(commandKey)) {
                    if (attemptReconnect()) {
                        run();
                        return;
                    }
                } else if ("exit".equalsIgnoreCase(commandKey)) {
                    running = false;
                } else if ("help".equalsIgnoreCase(commandKey)) {
                    System.out.println("Offline commands:");
                    System.out.println("  reconnect – try to connect to server");
                    System.out.println("  exit      – close the client");
                    System.out.println("  help      – show this message");
                } else {
                    System.out.println("  Command '" + commandKey + "' requires server connection.");
                    System.out.println("   Type 'reconnect' first, or 'help' for offline commands.");
                }
            } else {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }
        }
    }

    public boolean attemptReconnect() {
        if (networkReader != null) {
            networkReader.stop();
        }
        if (networkThread != null && networkThread.isAlive()) {
            try {
                networkThread.join(1000);
            } catch (InterruptedException ignored) {}
        }
        String host = connection.getHost();
        int port = connection.getPort();
        System.out.println(" Attempting to reconnect to " + host + ":" + port + "...");

        if (connection.connect(host, port)) {
            System.out.println(" Reconnected successfully!");
            try {
                String clientId = context.getClientId();
                String parentClientId = context.getParentClientId();
                HandshakeRequest handshake = new HandshakeRequest(clientId, parentClientId);
                connection.sendHandshake(handshake);
                CommandResponse handshakeResponse = connection.readResponse();
                if (handshakeResponse != null && handshakeResponse.success()){
                    restartNetworkReader();
                }
                return true;
            } catch (IOException e) {
                System.err.println("  Handshake failed after reconnect: " + e.getMessage());
                connection.disconnect();
                return false;
            }
        } else {
            System.err.println(" Reconnection failed. Check server status and try again.");
            return false;
        }
    }
    public void restartNetworkReader() {
        if (networkReader != null) networkReader.stop();
        if (networkThread != null && networkThread.isAlive()) {
            try { networkThread.join(500); } catch (InterruptedException ignored) {}
        }
        networkReader = new AsyncNetworkReader(connection.getSocketChannel(), reason -> {
            System.out.println("  Connection lost: " + reason);
            connection.setConnected(false);
            if (reason == DisconnectReason.PARENT_DOWN) {
                System.out.println("Parent exit. Exiting...");
                running = false;
                if (mainThread != null) mainThread.interrupt();
            }
        });

        networkThread = new Thread(networkReader);
        networkThread.setDaemon(true);
        networkThread.start();
        System.out.println(" Network reader restarted successfully.");
    }
}