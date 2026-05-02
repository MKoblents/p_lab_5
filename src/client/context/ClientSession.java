package client.context;

import client.command.SpawnClient;
import client.handlers.ResponseHandler;
import client.inputWorkers.InputManager;
import client.inputWorkers.Invoker;
import client.io.ConsoleBufferedScanner;
import client.network.AsyncNetworkReader;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import client.scripts.ScriptRunner;
import shared.dto.*;
import shared.enums.DisconnectReason;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientSession implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ClientSession.class);
    private volatile ScheduledExecutorService heartbeatScheduler;
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
    private volatile boolean awaitingLogin = true;

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
        this.spawnClientCommand = new SpawnClient(context, processManager);
    }

    public void run() throws IOException {
        System.out.println("Connected to server. Type 'help' for available commands or 'exit' to quit.");
        logger.info("Client session started for client: {}", context.getClientId());
        mainThread = Thread.currentThread();
        networkReader = new AsyncNetworkReader(connection.getSocketChannel(), reason -> {
            logger.info("Connection lost for client {}: reason={}", context.getClientId(), reason);
            System.out.println("Connection lost: " + reason);
            connection.setConnected(false);
            if (reason == DisconnectReason.PARENT_DOWN) {
                System.out.println("Parent client disconnected. Shutting down...");
                logger.info("Parent-down disconnect triggered shutdown for client: {}", context.getClientId());
                running = false;
                if (mainThread != null) mainThread.interrupt();
                close();
                System.exit(0);
            }
        });
        networkThread = new Thread(networkReader);
        networkThread.setDaemon(true);
        networkThread.start();

        Thread responseThread = new Thread(this::processResponsesLoop);
        responseThread.setDaemon(true);
        responseThread.start();
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-scheduler");
            t.setDaemon(true);
            return t;
        });
        Runnable heartbeatTask = () -> {
            try {
                CommandRequest request = new CommandRequest(
                        CommandRequest.CMD_HEARTBEAT,
                        null,
                        UUID.randomUUID().toString().substring(0, 8),
                        context.getClientId(),
                        context.getUserInfo());
                connection.sendRequest(request);
                logger.trace("Heartbeat sent for client: {}", context.getClientId());
            } catch (IOException | RuntimeException e) {
                logger.warn("Heartbeat failed for client {}: {}", context.getClientId(), e.getMessage());
            }
        };
        heartbeatScheduler.scheduleWithFixedDelay(heartbeatTask, 0, 5, TimeUnit.SECONDS);
        logger.debug("Heartbeat scheduler started for client: {}", context.getClientId());
        System.out.println("You should log in to execute any command. In any time you can relogin to another user using command 'log_in'.");
        invoker.runCommand("log_in");

        while (running) {
            if (context.isAwaitingForwardedInput()) {
                try { Thread.sleep(20); } catch (InterruptedException e) { break; }
                continue;
            }
            if (!networkReader.getForwardQueue().isEmpty()) {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
                continue;
            }
            String commandKey = null;
            try {
                String line = ((ConsoleBufferedScanner) inputManager.getReader()).pollNextLine(50);
                if (line != null && !line.trim().isEmpty() && !line.trim().startsWith("#")) {
//                    System.out.println(line);
                    inputManager.parseLine(line);
                    commandKey = inputManager.getCurrentCommandName();
                }
            } catch (IOException e) {
                logger.warn("Console poll error for client {}: {}", context.getClientId(), e.getMessage());
                break;
            }
            if (commandKey != null && !commandKey.isEmpty()) {
                synchronized (inputLock) {
                    if (!context.isAwaitingForwardedInput() && networkReader.getForwardQueue().isEmpty()) {
                        CommandRequest request = invoker.runCommand(commandKey);
                        if (request != null) {
                            if (request.userInfo() != null || commandKey.equals("log_in")){
                                try {
                                    if (!connection.isConnected()) {
                                        System.out.println("Not connected to server. Cannot send request.");
                                        logger.debug("Command '{}' skipped: client not connected", commandKey);
                                    } else {
                                        connection.sendRequest(request);
                                        logger.debug("Request sent for command '{}'", commandKey);
                                    }
                                } catch (IOException e) {
                                    System.err.println("Network error while sending request: " + e.getMessage());
                                    logger.error("Failed to send request for command '{}': {}", commandKey, e.getMessage());
                                }
                            }else {
                                System.out.println("You should log in before execute any command");
                            }
                        }
                    }
                }
            } else {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }
        }
    }
    private void handleLoginResponse(CommandResponse response) {
        if (response == null) return;

        if (response.message() != null && response.message().contains("Log in success") && response.success()) {
            context.setUserInfo((UserInfo) response.result());
            System.out.println("Login successful.");
            awaitingLogin = false;
        } else if (response.message() != null && response.message().contains("Log in")) {
            System.out.println("Login failed: " + response.message());
            System.out.print("Retry? (y/n): ");
            try {
                String retry = ((ConsoleBufferedScanner) inputManager.getReader()).getInputString();
                if ("y".equalsIgnoreCase(retry)) {
                    awaitingLogin = true;
                    invoker.runCommand("log_in");
                } else {
                    awaitingLogin = false;
                }
            } catch (IOException e) { awaitingLogin = false; }
        } else {
            handleResponse(response);
        }
    }

    private void processResponsesLoop() {
        while (running) {
            CommandResponse response = networkReader.getResponseQueue().poll();
            if (response != null) {
                String message = response.message();
                if (message != null) {
                    if (DisconnectReason.KILLED_BY_PARENT.name().equals(message)) {
                        System.out.println("Session terminated by parent client. Exiting...");
                        logger.info("Client {} terminated by parent", context.getClientId());
                        running = false;
                        connection.setConnected(false);
                        if (mainThread != null) mainThread.interrupt();
                        close();
                        System.exit(0);
                        continue;
                    }
                    if (DisconnectReason.PARENT_DOWN.name().equals(message)) {
                        System.out.println("Parent client exited. Shutting down...");
                        logger.info("Parent-down signal received for client {}", context.getClientId());
                        running = false;
                        connection.setConnected(false);
                        if (mainThread != null) mainThread.interrupt();
                        continue;
                    }
                }
                handleResponse(response);
            }
            CommandRequest forwarded = networkReader.getForwardQueue().poll();
            if (forwarded != null) {
                synchronized (inputLock) {
                    if (forwarded.args() instanceof ForwardCommandObject fco) {
                        handleForwardedCommand(fco.commandKey());
                    } else {
                        logger.warn("Received unknown forwarded object type: {}", forwarded.args() != null ? forwarded.args().getClass().getSimpleName() : "null");
                    }
                }
            }
            try { Thread.sleep(50); } catch (InterruptedException e) { break; }
        }
    }

    private void handleResponse(CommandResponse response) {
        if (response == null) {
            logger.warn("handleResponse called with null response");
            return;
        }
        String requestId = response.requestId();
        String message = response.message();
        if ("spawn_client".equals(requestId) ||
                (message != null && message.contains("Child client created"))) {
            spawnClientCommand.handleResponse(response, context);
        } else {
            responseHandler.handle(response);
        }
    }

    private void handleForwardedCommand(String commandKey) {
        logger.info("Received forwarded command '{}' for client {}", commandKey, context.getClientId());
        System.out.println("[Received command from parent: " + commandKey + "]");
        CommandRequest localRequest = invoker.runServerCommand(commandKey, SideFlag.FORWARDED);
        if (localRequest != null) {
            try {
                connection.sendRequest(localRequest);
                logger.debug("Forwarded command '{}' sent to server", commandKey);
            } catch (IOException e) {
                logger.error("Failed to send forwarded command '{}': {}", commandKey, e.getMessage());
                System.err.println("Network error while executing forwarded command: " + commandKey);
            }
        } else {
            logger.warn("Failed to build request for forwarded command: {}", commandKey);
            System.err.println("Failed to build request for forwarded command: " + commandKey);
        }
    }

    @Override
    public void close() {
        logger.info("Closing client session for client: {}", context.getClientId());
        running = false;
        if (mainThread != null) mainThread.interrupt();
        if (networkReader != null) networkReader.stop();
        connection.disconnect();
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdownNow();
            try {
                if (!heartbeatScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    logger.warn("Heartbeat scheduler did not terminate within timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Interrupted while waiting for heartbeat scheduler termination");
            }
        }
        logger.debug("Client session closed for client: {}", context.getClientId());
    }
    public void runOffline() throws IOException {
        System.out.println("OFFLINE MODE: No server connection available.");
        System.out.println("Available commands: help, exit, reconnect");
        logger.info("Client {} entered offline mode", context.getClientId());
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
                    logger.info("Exit command received in offline mode");
                    running = false;
                } else if ("help".equalsIgnoreCase(commandKey)) {
                    System.out.println("Offline commands:");
                    System.out.println("  reconnect - try to connect to server");
                    System.out.println("  exit      - close the client");
                    System.out.println("  help      - show this message");
                } else {
                    System.out.println("Command '" + commandKey + "' requires server connection.");
                    System.out.println("Type 'reconnect' first, or 'help' for offline commands.");
                    logger.debug("Command '{}' ignored in offline mode", commandKey);
                }
            } else {
                try { Thread.sleep(50); } catch (InterruptedException e) { break; }
            }
        }
    }

    public boolean attemptReconnect() {
        logger.info("Attempting reconnection for client {}", context.getClientId());
        if (networkReader != null) {
            networkReader.stop();
        }
        if (networkThread != null && networkThread.isAlive()) {
            try {
                networkThread.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        String host = connection.getHost();
        int port = connection.getPort();
        System.out.println("Attempting to reconnect to " + host + ":" + port + "...");
        if (connection.connect(host, port)) {
            System.out.println("Reconnected successfully.");
            logger.info("Reconnection successful for client {} to {}:{}", context.getClientId(), host, port);
            try {
                String clientId = context.getClientId();
                String parentClientId = context.getParentClientId();
                HandshakeRequest handshake = new HandshakeRequest(clientId, parentClientId);
                connection.sendHandshake(handshake);
                logger.debug("Handshake request sent for client {}", clientId);

                CommandResponse handshakeResponse = connection.readResponse();
                if (handshakeResponse != null && handshakeResponse.success()) {
                    restartNetworkReader();
                    logger.info("Handshake confirmed for client {}", clientId);
                    return true;
                } else {
                    String errorMsg = handshakeResponse != null ? handshakeResponse.message() : "No response";
                    logger.warn("Handshake failed for client {}: {}", clientId, errorMsg);
                    System.err.println("Handshake failed: " + errorMsg);
                    connection.disconnect();
                    return false;
                }
            } catch (IOException e) {
                logger.error("IO error during handshake for client {}: {}", context.getClientId(), e.getMessage());
                System.err.println("Handshake failed due to communication error: " + e.getMessage());
                connection.disconnect();
                return false;
            }
        } else {
            logger.warn("Reconnection failed for client {}: unable to reach {}:{}", context.getClientId(), host, port);
            System.err.println("Reconnection failed. Please ensure the server is running and the address is correct.");
            return false;
        }
    }

    public void restartNetworkReader() {
        logger.debug("Restarting network reader for client {}", context.getClientId());
        if (networkReader != null) networkReader.stop();
        if (networkThread != null && networkThread.isAlive()) {
            try { networkThread.join(500); } catch (InterruptedException ignored) {}
        }

        networkReader = new AsyncNetworkReader(connection.getSocketChannel(), reason -> {
            System.out.println("Connection lost: " + reason);
            logger.info("Connection lost for client {}: reason={}", context.getClientId(), reason);
            connection.setConnected(false);
            if (reason == DisconnectReason.PARENT_DOWN) {
                System.out.println("Parent client disconnected. Shutting down...");
                logger.info("Parent-down disconnect triggered for client {}", context.getClientId());
                running = false;
                if (mainThread != null) mainThread.interrupt();
            }
        });

        networkThread = new Thread(networkReader);
        networkThread.setDaemon(true);
        networkThread.start();
        System.out.println("Network reader restarted successfully.");
        logger.info("Network reader restarted for client {}", context.getClientId());
    }
}