package client.command;

import client.context.ClientContext;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class SpawnClient implements ClientCommand {
    private static final Logger logger = LoggerFactory.getLogger(SpawnClient.class);
    private final ClientContext context;
    private final ClientProcessManager processManager;

    public SpawnClient(ClientContext context,
                       ClientProcessManager processManager) {
        this.context = context;
        this.processManager = processManager;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        logger.debug("Executing spawn_client command");
        return RequestsFactory.createSimple("spawn_client");
    }

    public void handleResponse(CommandResponse response, ClientContext parentContext) {
        if (response == null) {
            System.err.println("Error: No response received from server for spawn request.");
            logger.warn("handleResponse called with null response for spawn_client");
            return;
        }
        if (response.success() && response.clientId() != null) {
            String childClientId = response.clientId();
            logger.info("Received spawn response, childId={}", childClientId);
            try {
                processManager.spawnChild(
                        childClientId,
                        parentContext.getClientId()
                );
                parentContext.addChild(childClientId);
                logger.info("Child {} added to context", childClientId);
                System.out.println("Spawned child client: " + childClientId);
            } catch (Exception e) {
                logger.error("Failed to spawn child {}", childClientId, e);
                System.err.println("Error: Failed to spawn child client. Details: " + e.getMessage());
            }
        } else {
            String errorMessage = response.message();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = response.success()
                        ? "Operation completed but no client ID was returned."
                        : "Server rejected the spawn request for an unspecified reason.";
            }
            logger.warn("Server rejected spawn request: {}", errorMessage);
            System.err.println("Error: " + errorMessage);
        }
    }

    private String generateRequestId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }
}