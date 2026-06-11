package server.commands;
import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;

import java.sql.SQLException;

public class RemoveByIdCommand implements  Command{
    private String helpInformation = "remove_by_id id : удалить элемент из коллекции по его id";
    private CollectionService collectionService;
    public RemoveByIdCommand(CollectionService collectionService){
        this.collectionService = collectionService;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        Long id = (Long) commandRequest.args();
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Valid ID required for remove_by_id, got: " + id);
        }
        try {
            if (!collectionService.isIdInCollection(id)) {
                return new CommandResponse(
                        false, id,
                        "Element with ID " + id + " does not exist", commandRequest.requestId(), commandRequest.clientId());
            }
            collectionService.remove(id, commandRequest.userInfo().name());
            return new CommandResponse(
                    true, id,
                    "Element with ID " + id + " removed successfully",
                    commandRequest.requestId(),
                    commandRequest.clientId()

            );
        }catch (SQLException e){
            return new CommandResponse(false,id,"SpaceMarine with id "+id.toString()+" removing failed because of db access "+e.getMessage(), commandRequest.requestId(),commandRequest.clientId());
        }
    }
}
