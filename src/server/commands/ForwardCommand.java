package server.commands;

import server.manager.ClientRegistry;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.dto.ForwardCommandObject;

public class ForwardCommand implements Command{
    private String helpInformation = "";
    private ClientRegistry clientRegistry;
    public ForwardCommand(ClientRegistry clientRegistry){
        this.clientRegistry = clientRegistry;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }


    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        ForwardCommandObject fco = (ForwardCommandObject) commandRequest.args();
        String parentId = fco.parentId();
        String childId = fco.childId();
        if (clientRegistry.exists(childId) && clientRegistry.isParentOf(parentId, childId)){
            clientRegistry.getPendingCommandQueue().addPendingCommand(childId, commandRequest);
            return new CommandResponse(true, null, "Command sent to child", commandRequest.requestId(), parentId);
        }
        return new CommandResponse(false, null, "Failed to send to child", commandRequest.requestId(), parentId);
    }
}
