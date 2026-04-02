// client/command/ForwardCommand.java
package client.command;

import client.context.ClientContext;
import client.hierarchy.PeerConnection;
import shared.dto.CommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardCommand implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(ForwardCommand.class);

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

    @Override
    public CommandRequest execute() {
        logger.info("Forwarding command '{}' to client {}", actualCommand, targetClientId);

        try {
            Integer targetPort = getTargetPort(targetClientId);
            if (targetPort == null) {
                System.err.println("Error: Unknown client ID: " + targetClientId);
                return null;
            }
            String message = String.format("FORWARD:%s:%s",
                    context.getClientId(),
                    actualCommand);

            peerConnection.sendToPeer("localhost", targetPort, message);

            System.out.println("✓ Command forwarded to client " + targetClientId);
            return null;

        } catch (Exception e) {
            logger.error("Failed to forward command", e);
            System.err.println("✗ Failed to forward command: " + e.getMessage());
            return null;
        }
    }

    private Integer getTargetPort(String clientId) {
        Integer port = context.getChildPeerPort(clientId);
        if (port != null) return port;
        return null;
    }
}