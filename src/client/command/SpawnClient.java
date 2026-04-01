package client.command;

import client.context.ClientContext;
import client.network.ConnectionManager;
import client.process.ClientProcessManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class SpawnClient implements ClientCommand{
    private final ClientContext context;
    private final ConnectionManager connection;
    private final ClientProcessManager processManager;
    public SpawnClient(ClientContext context,
                       ConnectionManager connection,
                       ClientProcessManager processManager) {
        this.context = context;
        this.connection = connection;
        this.processManager = processManager;
    }
    public CommandRequest execute() {
        // ← Эта команда отправляется на сервер для генерации clientId
        // Сервер вернет CommandResponse с clientId в новом поле
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
            try {
                processManager.spawnChild(
                        childClientId,
                        parentContext.getClientId(),
                        parentContext.getPeerPort()
                );
                parentContext.addChild(childClientId);
                System.out.println("✓ Spawned child client: " + childClientId);
            } catch (Exception e) {
                System.err.println("✗ Failed to spawn child: " + e.getMessage());
            }
        } else {
            System.err.println("✗ Server rejected spawn request: " + response.message());
        }
    }
    private String generateRequestId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
//    TODO understand
    }

}
