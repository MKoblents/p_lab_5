package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

public class CouldBeUpdatedCommand implements Command{
    private CollectionService collectionService;
    public CouldBeUpdatedCommand(CollectionService collectionService){
        this.collectionService = collectionService;
    }
    @Override
    public String getHelpInformation() {
        return "";
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        long id = (long) commandRequest.args();
        if (collectionService.getUpdatingSpaceMarines().contains(id)){
            return new CommandResponse(true, false,"can't update right now: another client's updating it", commandRequest.requestId(), commandRequest.clientId());
        }
        collectionService.addUpdating(id);
        return  new CommandResponse(true, true,"can update right now", commandRequest.requestId(), commandRequest.clientId());
    }
}
