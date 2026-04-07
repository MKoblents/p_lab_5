package server.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.client.ConnectedClient;
import shared.enums.ClientState;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background monitor that detects stale/unresponsive clients.
 * Runs periodic scans and updates client state to OFFLINE if heartbeat timeout exceeded.
 */
public class ClientHealthMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ClientHealthMonitor.class);

    private final ClientRegistry clientRegistry;
    private final ScheduledExecutorService scanner;
    private final Duration heartbeatTimeout;
    private final Duration scanInterval;

    private volatile boolean running = false;

    /**
     * @param clientRegistry shared registry of connected clients
     * @param clientHeartbeatInterval expected interval between client heartbeats (e.g., 5s)
     */
    public ClientHealthMonitor(ClientRegistry clientRegistry, long clientHeartbeatInterval, TimeUnit unit) {
        this.clientRegistry = clientRegistry;
        this.heartbeatTimeout = Duration.ofSeconds(clientHeartbeatInterval).multipliedBy(3);
        this.scanInterval = Duration.ofSeconds(clientHeartbeatInterval).multipliedBy(3);
        this.scanner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "client-health-scanner");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts the background scanning task.
     */
    public void start() {
        if (running) {
            logger.warn("HealthMonitor already running");
            return;
        }
        running = true;
        scanner.scheduleWithFixedDelay(this::scanClients, 0, scanInterval.toSeconds(), TimeUnit.SECONDS);
        logger.info("ClientHealthMonitor started (timeout={}, interval={})", heartbeatTimeout, scanInterval);
    }

    /**
     * Stops the monitor and cleans up resources.
     */
    public void stop() {
        if (!running) return;
        running = false;
        scanner.shutdownNow();
        try {
            if (!scanner.awaitTermination(2, TimeUnit.SECONDS)) {
                logger.warn("HealthMonitor scanner did not terminate gracefully");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while stopping HealthMonitor");
        }
        logger.info("ClientHealthMonitor stopped");
    }

    /**
     * Scans all registered clients and marks stale ones as OFFLINE.
     * Does NOT remove clients from registry — that's handled by ClientHandler on socket close.
     */
    private void scanClients() {
        Instant now = Instant.now();
        int markedOffline = 0;

        for (ConnectedClient client : clientRegistry.getAllClients()) {
            if (client.getClientStatus().clientState() == ClientState.OFFLINE) {
                continue;
            }

            Duration idleTime = Duration.between(client.getClientStatus().lastHeartbeat(), now);
            if (idleTime.compareTo(heartbeatTimeout) > 0) {
                client.markOffline();
                markedOffline++;
                logger.debug("Client {} marked OFFLINE (idle for {}s)",
                        client.getClientStatus().clientId(), idleTime.toSeconds());
            }
        }

        if (markedOffline > 0) {
            logger.info("Marked {} client(s) as OFFLINE during health scan", markedOffline);
        }
    }

    /**
     * Helper: explicitly mark a client as ONLINE (e.g., after successful command).
     * Thread-safe via ConnectedClient internal state.
     */
    public void markClientOnline(String clientId) {
        clientRegistry.getClient(clientId).ifPresent(ConnectedClient::markOnline);
    }

    /**
     * Returns current count of ONLINE clients (for quick metrics).
     */
    public long getOnlineClientCount() {
        return clientRegistry.getAllClients().stream()
                .filter(c -> c.getClientStatus().clientState() == ClientState.ONLINE)
                .count();
    }
}