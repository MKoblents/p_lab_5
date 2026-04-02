package server.commands;

import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

public class RemoveGreaterCommand implements Command {
    private final CollectionManager collectionManager;
    private String helpInformation = "remove_greater {element} : удалить из коллекции все элементы, превышающие заданный";

    public RemoveGreaterCommand(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
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
        collectionManager.removeGreater(spaceMarine);
        return new CommandResponse(
                true,
                spaceMarine, "Removed succsessfully", commandRequest.requestId(), commandRequest.clientId());
    }
}
