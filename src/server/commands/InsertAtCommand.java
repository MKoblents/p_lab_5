package server.commands;

import client.inputWorkers.InputManager;
import server.manager.CollectionManager;
import server.validator.Validator;
import shared.dto.CommandRequest;
import shared.dto.CommandResponse;
import shared.models.SpaceMarine;

public class InsertAtCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation = "insert_at index {element} : добавить новый элемент в заданную позицию";
    private Validator validator;
    public InsertAtCommand(CollectionManager collectionManager, Validator validator){
        this.collectionManager = collectionManager;
        this.validator = validator;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public CommandResponse execute(CommandRequest commandRequest) {
        SpaceMarine spaceMarine = (SpaceMarine) commandRequest.getData();
        validator.spaceMarineValidate(spaceMarine);
        collectionManager.addItem(inputManager.getLastInt(), spaceMarine);

    }
}
