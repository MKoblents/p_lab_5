package client.hierarchy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PeerConnection implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);
    private ServerSocket peerListener;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public int startListening(Consumer<String> onMessageReceived) throws IOException {
        peerListener = new ServerSocket(0);
        int port = peerListener.getLocalPort();
        logger.info("P2P listener started on port {}", port);

        executor.submit(() -> {
            while (running && !peerListener.isClosed()) {
                try {
                    Socket clientSocket = peerListener.accept();
                    logger.debug("P2P connection accepted from {}",
                            clientSocket.getRemoteSocketAddress());
                    executor.submit(() -> handleIncomingMessage(clientSocket, onMessageReceived));
                } catch (IOException e) {
                    if (running) {
                        logger.error("P2P accept error", e);
                    }
                }
            }
        });
        return port;
    }
    public void registerPendingRequest(String requestId, CompletableFuture<String> future) {
        pendingRequests.put(requestId, future);
        scheduler.schedule(() -> {
            CompletableFuture<String> f = pendingRequests.remove(requestId);
            if (f != null && !f.isDone()) {
                f.completeExceptionally(new TimeoutException("Response timeout for request: " + requestId));
            }
        }, 35, TimeUnit.SECONDS);
    }
    public void sendForwardResult(String toClientId, int port, String requestId, boolean success, String result) {
        String message = String.format("FORWARD_RESULT:%s:%b:%s", requestId, success, result);
        try {
            sendToPeer("localhost", port, message);
            logger.debug("Forward result sent to {}:{} - success={}", toClientId, port, success);
        } catch (IOException e) {
            logger.error("Failed to send forward result to {}: {}", toClientId, e.getMessage());
        }
    }

    public void sendForwardCommand(String toClientId, int port, String fromClientId, String requestId, String command) throws IOException {
        String message = String.format("FORWARD:%s:%s:%s", fromClientId, requestId, command);
        sendToPeer("localhost", port, message);
        logger.debug("Forward command sent to {}:{} - command: {}", toClientId, port, command);
    }

    private void handleIncomingMessage(Socket socket, Consumer<String> onMessageReceived) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String message = reader.readLine();
            if (message != null) {
                logger.info("P2P message received: {}", message);
                if (message.startsWith("FORWARD_RESULT:")) {
                    handleForwardResult(message);
                }
                else if (message.startsWith("FORWARD:")) {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }
                else {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("P2P read error", e);
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
    private void handleForwardResult(String message) {
        String[] parts = message.split(":", 4);
        if (parts.length >= 4) {
            String originalRequestId = parts[1];
            boolean success = Boolean.parseBoolean(parts[2]);
            String result = parts[3];

            logger.debug("Received forward result for requestId: {}, success: {}", originalRequestId, success);

            CompletableFuture<String> future = pendingRequests.remove(originalRequestId);
            if (future != null) {
                if (success) {
                    future.complete(result);
                } else {
                    future.completeExceptionally(new RuntimeException(result));
                }
            } else {
                logger.warn("No pending request found for requestId: {}", originalRequestId);
            }
        } else {
            logger.warn("Malformed FORWARD_RESULT message: {}", message);
        }
    }
    public void sendToPeer(String host, int port, String message) throws IOException {
        logger.debug("Sending P2P message to {}:{} - {}", host, port, message);
        try (Socket socket = new Socket(host, port);
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            writer.println(message);
        }
    }
    @Override
    public void close() {
        running = false;
        scheduler.shutdown();
        try {
            if (peerListener != null && !peerListener.isClosed()) {
                peerListener.close();
            }
        } catch (IOException e) {
            logger.error("Error closing P2P listener", e);
        }
        executor.shutdownNow();
    }
}