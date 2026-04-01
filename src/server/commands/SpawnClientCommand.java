package server.commands;

import server.manager.ClientRegistry;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.util.UUID;

public class SpawnClientCommand implements Command{
    private final ClientRegistry clientRegistry;
    private final String HELP_INFO = "spawn_client : создать нового клиента (дочерний процесс)";
    public SpawnClientCommand(ClientRegistry clientRegistry){
        this.clientRegistry = clientRegistry;
    }

    @Override
    public String getHelpInformation() {
        return HELP_INFO;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        String childClientId = UUID.randomUUID().toString().substring(0, 8);
        String parentClientId = commandRequest.clientId();
        clientRegistry.register(childClientId, parentClientId);
        return new CommandResponse(true, null, "Child client created: "+childClientId, commandRequest.requestId(), childClientId);
    }
}
