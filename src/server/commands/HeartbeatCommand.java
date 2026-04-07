package server.commands;

import server.manager.ClientRegistry;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class HeartbeatCommand implements Command{
    private ClientRegistry clientRegistry;
    public HeartbeatCommand(ClientRegistry clientRegistry){
        this.clientRegistry = clientRegistry;
    }

    @Override
    public String getHelpInformation() {
        return "";
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        String clientId = commandRequest.clientId();
        clientRegistry.updateHeartbeat(clientId);
        return new CommandResponse(true, null, "ok", commandRequest.requestId(), clientId);
    }
}
