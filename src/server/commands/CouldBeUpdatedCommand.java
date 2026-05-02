package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.sql.SQLException;

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
        String name = commandRequest.userInfo().name();
        try {
            String realOwner = collectionService.getOwnerName(id);
            if (collectionService.isAncestorOrSelf(name,realOwner)) {
                if (!collectionService.getUpdatingSpaceMarines().contains(id)) {
                    collectionService.addUpdating(id);
                    return new CommandResponse(true, true, "can update right now", commandRequest.requestId(), commandRequest.clientId());
                }
                return new CommandResponse(true, false, "can't update right now: another client's updating it", commandRequest.requestId(), commandRequest.clientId());
            }
            return new CommandResponse(true, false, "can't update. You are not the owner", commandRequest.requestId(), commandRequest.clientId());
        }catch (SQLException e){
            return new CommandResponse(false, false, e.getMessage(), commandRequest.requestId(), commandRequest.clientId());
        }
    }
}
