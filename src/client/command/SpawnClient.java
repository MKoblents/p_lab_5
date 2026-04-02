package client.command;

import client.context.ClientContext;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class SpawnClient implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(SpawnClient.class);
    private final ClientContext context;
    private final ConnectionManager connection;
    private final ClientProcessManager processManager;

    public SpawnClient(ClientContext context, ConnectionManager connection,
                       ClientProcessManager processManager) {
        this.context = context;
        this.connection = connection;
        this.processManager = processManager;
    }

    @Override
    public CommandRequest execute() {
        logger.debug("Executing spawn_client command, peerPort={}", context.getPeerPort());
        return new CommandRequest(
                "spawn_client",
                context.getPeerPort(),
                generateRequestId(),
                context.getClientId()
        );
    }

    public void handleResponse(CommandResponse response, ClientContext parentContext) {
        if (response.success() && response.clientId() != null) {
            String childClientId = response.clientId();
            logger.info("Received spawn response, childId={}", childClientId);
            try {
                processManager.spawnChild(
                        childClientId,
                        parentContext.getClientId(),
                        parentContext.getPeerPort()
                );
                parentContext.addChild(childClientId);
                logger.info("Child {} added to context", childClientId);
                System.out.println("✓ Spawned child client: " + childClientId);
            } catch (Exception e) {
                logger.error("Failed to spawn child {}", childClientId, e);
                System.err.println("✗ Failed to spawn child: " + e.getMessage());
            }
        } else {
            logger.warn("Server rejected spawn request: {}", response.message());
            System.err.println("✗ Server rejected spawn request: " + response.message());
        }
    }

    private String generateRequestId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
//    TODO understand
    }
}