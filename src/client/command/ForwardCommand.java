// client/command/ForwardCommand.java
package client.command;

import client.context.ClientContext;
import client.hierarchy.PeerConnection;
import shared.dto.CommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ForwardCommand implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(ForwardCommand.class);
    private static final int RESPONSE_TIMEOUT_SECONDS = 30;

    private final String targetClientId;
    private final String actualCommand;
    private final ClientContext context;
    private final PeerConnection peerConnection;

    public ForwardCommand(String targetClientId, String actualCommand,
                          ClientContext context, PeerConnection peerConnection) {
        this.targetClientId = targetClientId;
        this.actualCommand = actualCommand;
        this.context = context;
        this.peerConnection = peerConnection;
    }

    // client/command/ForwardCommand.java

    @Override
    public CommandRequest execute() {
        logger.info("Forwarding command '{}' to client {}", actualCommand, targetClientId);
        System.out.println("\n📤 Forwarding to [" + targetClientId + "]: " + actualCommand);

        try {
            Integer targetPort = getTargetPort(targetClientId);
            if (targetPort == null) {
                System.err.println("Error: Unknown client ID: " + targetClientId);
                return null;
            }

            String requestId = UUID.randomUUID().toString().substring(0, 8);

            // Отправляем команду (fire-and-forget)
            String message = String.format("FORWARD:%s:%s:%s",
                    context.getClientId(),
                    requestId,
                    actualCommand);

            peerConnection.sendToPeer("localhost", targetPort, message);

            System.out.println("✓ Command sent to client " + targetClientId);
            System.out.println("  Result will be displayed on that client's console.");

            return null;

        } catch (Exception e) {
            logger.error("Failed to forward command", e);
            System.err.println("✗ Failed to forward command: " + e.getMessage());
            return null;
        }
    }

    private Integer getTargetPort(String clientId) {
        Integer port = context.getChildPeerPort(clientId);
        if (port != null) {
            logger.debug("Found port {} for child client {}", port, clientId);
            return port;
        }
        logger.warn("No port found for client ID: {}", clientId);
        return null;
    }
}