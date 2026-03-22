package server.commands;

import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

public class InsertAtCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "insert_at index {element} : добавить новый элемент в заданную позицию";
    private InputManager inputManager;
    public InsertAtCommand(CollectionManager collectionManager, InputManager inputManager){
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        SpaceMarine spaceMarine = inputManager.getInputSpaceMarine();
        inputManager.getValidator().spaceMarineValidate(spaceMarine);
        collectionManager.addItem(inputManager.getLastInt(), spaceMarine);

    }
}
