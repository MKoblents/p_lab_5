package server.commands;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class CouldBeUpdatedCommand implements Command{
    private CollectionManager collectionManager;
    public CouldBeUpdatedCommand(CollectionManager collectionManager){
        this.collectionManager = collectionManager;
    }
    @Override
    public String getHelpInformation() {
        return "";
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        long id = (long) commandRequest.args();
        if (collectionManager.getUpdatingSpaceMarines().contains(id)){
            return new CommandResponse(true, false,"can't update right now", commandRequest.requestId(), commandRequest.clientId());
        }
        collectionManager.addUpdating(id);
        return  new CommandResponse(true, true,"can update right now", commandRequest.requestId(), commandRequest.clientId());
    }
}
