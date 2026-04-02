package client.hierarchy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class PeerConnection implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(PeerConnection.class);
    private ServerSocket peerListener;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running = true;

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

    private void handleIncomingMessage(Socket socket, Consumer<String> onMessageReceived) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String message = reader.readLine();
            if (message != null) {
                logger.info("P2P message received: {}", message);
                onMessageReceived.accept(message);
            }
        } catch (IOException e) {
            logger.warn("P2P read error", e);
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
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