package server.commands;

import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

public class RemoveGreaterCommand implements Command{
    private final InputManager inputManager;
    private final CollectionManager collectionManager;
    private String helpInformation = "remove_greater {element} : удалить из коллекции все элементы, превышающие заданный";
    public RemoveGreaterCommand(CollectionManager collectionManager,InputManager inputManager){
        this.inputManager = inputManager;
        this.collectionManager = collectionManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        SpaceMarine spaceMarine = inputManager.getInputSpaceMarine();
        inputManager.getValidator().spaceMarineValidate(spaceMarine);
//        collectionManager.addItem(spaceMarine);
        collectionManager.removeGreater(spaceMarine);    }
}
