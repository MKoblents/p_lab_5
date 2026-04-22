package server.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.manager.ClientRegistry;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class KillClientCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(KillClientCommand.class);
    private String helpInformation = "kill_client <client_id> - terminate a child client process";
    private final ClientRegistry clientRegistry;
    public KillClientCommand(ClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        if (commandRequest == null) {
            logger.warn("Received null command request for kill_client");
            return new CommandResponse(false, null, "Error: Invalid request format", null, null);
        }
        String parentId = commandRequest.clientId();
        Object args = commandRequest.args();

        if (!(args instanceof String)) {
            logger.warn("Invalid argument type for kill_client from client: {}", parentId);
            return new CommandResponse(false, null, "Error: Expected client ID string, received invalid format",
                    commandRequest.requestId(), parentId);
        }
        String clientId = (String) args;
        if (clientId == null || clientId.trim().isEmpty()) {
            logger.warn("kill_client command received with empty client ID from parent: {}", parentId);
            return new CommandResponse(false, null, "Error: Client ID cannot be empty",
                    commandRequest.requestId(), parentId);
        }
        logger.info("Kill request received: parent={} target={}", parentId, clientId);
        if (clientRegistry.isParentOf(parentId, clientId)) {
            try {
                clientRegistry.unregister(clientId);
                logger.info("Client {} successfully terminated by parent {}", clientId, parentId);
                return new CommandResponse(true, clientId, "Client " + clientId + " terminated successfully.",
                        commandRequest.requestId(), parentId);
            } catch (Exception e) {
                logger.error("Failed to terminate client {}: {}", clientId, e.getMessage(), e);
                return new CommandResponse(false, clientId, "Error: Failed to terminate client. Details: " + e.getMessage(),
                        commandRequest.requestId(), parentId);
            }
        } else {
            logger.warn("Authorization failed: parent {} cannot terminate client {} (invalid relationship)", parentId, clientId);
            return new CommandResponse(false, clientId, "Error: Cannot terminate client " + clientId + ". Invalid parent-child relationship or client not found.",
                    commandRequest.requestId(), parentId);
        }
    }
}