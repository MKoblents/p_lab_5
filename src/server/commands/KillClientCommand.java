package server.commands;

import client.command.KillClient;
import server.manager.ClientRegistry;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class KillClientCommand implements Command{
    private ClientRegistry clientRegistry;
    private String helpInformation = "kill_client: завершить процесс дочернего клиента";
    public KillClientCommand(ClientRegistry clientRegistry){
        this.clientRegistry = clientRegistry;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        String parentId = commandRequest.clientId();
        String clientId = (String) commandRequest.args();
        if (clientRegistry.isParentOf(parentId, clientId)){
            clientRegistry.unregister(clientId);
            return new CommandResponse(true, clientId, "Killed client" + clientId, commandRequest.requestId(), parentId);
        } return new CommandResponse(false, clientId, "Can't kill client " + clientId, commandRequest.requestId(), parentId);
    }
}
