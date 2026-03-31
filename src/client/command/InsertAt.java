package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.Validator;
import shared.dto.CommandRequest;
import shared.models.SpaceMarine;

public class InsertAt implements ClientCommand{
    private InputManager inputManager;
    public InsertAt(InputManager inputManager){
        this.inputManager = inputManager;
    }
    @Override
    public CommandRequest execute() {
        int index = inputManager.getLastInt();
        if (index<0){
            System.err.println("Error: Valid ID required for remove_by_id");
            return null;
        }
        SpaceMarine marine = inputManager.getInputSpaceMarine();
        if (marine == null){
            System.err.println("Error: Failed to parse SpaceMarine from XML");
            return null;
        }
        Validator.spaceMarineValidate(marine);
        return RequestsFactory.createIdMarine("insert_at", index, marine);
    }
}
