package server.commands;

import server.manager.CollectionManager;
import server.validator.Validator;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

public class RemoveGreaterCommand implements Command {
    private final CollectionManager collectionManager;
    private Validator validator;
    private String helpInformation = "remove_greater {element} : удалить из коллекции все элементы, превышающие заданный";

    public RemoveGreaterCommand(CollectionManager collectionManager, Validator validator) {
        this.collectionManager = collectionManager;
        this.validator = validator;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        Object data = commandRequest.getData();
        if (!(data instanceof SpaceMarine spaceMarine)) {
            throw new IllegalArgumentException("Expected SpaceMarine argument for remove_greater");
        }
        validator.spaceMarineValidate(spaceMarine);
        collectionManager.removeGreater(spaceMarine);
        return new CommandResponse(
                true,
                spaceMarine, null);
    }
}
