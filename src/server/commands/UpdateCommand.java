package server.commands;

import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

public class UpdateCommand implements Command{
    private String helpInformation = "update : обновить значение элемента коллекции, id которого равен заданному";
    private CollectionManager collectionManager;
    private InputManager inputManager;
    public UpdateCommand(CollectionManager collectionManager, InputManager inputManager){
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        long id = inputManager.getLastLong();
        if (!collectionManager.isIdInCollection(id)){
            System.err.println("Your id is missing in collection.");
            return;
        }
        SpaceMarine spaceMarine = collectionManager.getSpaceMarineById(id);
        SpaceMarine spaceMarineInput = inputManager.getInputSpaceMarine();
        inputManager.getValidator().spaceMarineValidate(spaceMarineInput);
        spaceMarineInput.setId(id);
        collectionManager.replace(spaceMarine, spaceMarineInput);
    }
}
