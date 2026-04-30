package server.commands;

import server.manager.CollectionService;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

import java.sql.SQLException;

public class RemoveGreaterCommand implements Command {
    private final CollectionService collectionService;
    private String helpInformation = "remove_greater {element} : удалить из коллекции все элементы, превышающие заданный";

    public RemoveGreaterCommand(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        Object data = commandRequest.args();
        if (!(data instanceof SpaceMarine spaceMarine)) {
            throw new IllegalArgumentException("Expected SpaceMarine argument for remove_greater");
        }
        try {
            collectionService.removeGreater(spaceMarine, commandRequest.userInfo().name());
            return new CommandResponse(
                    true,
                    spaceMarine, "Removed succsessfully", commandRequest.requestId(), commandRequest.clientId());
        }catch (SQLException e){
            return new CommandResponse(false,spaceMarine,"SpaceMarine removing failed because of db access "+e.getMessage(), commandRequest.requestId(),commandRequest.clientId());
        }
    }
}
