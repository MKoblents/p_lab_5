package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import client.utils.Validator;
import shared.dto.CommandRequest;
import shared.models.SpaceMarine;

public class RemoveGreater implements ClientCommand{
    private InputManager inputManager;
    public RemoveGreater(InputManager inputManager){
        this.inputManager=inputManager;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        SpaceMarine spaceMarine = inputManager.getInputSpaceMarine();
        if (spaceMarine == null) {
            System.err.println("Error: Failed to parse SpaceMarine from XML");
            return null;
        }
        Validator.spaceMarineValidate(spaceMarine);
        return RequestsFactory.withMarine("remove_greater", spaceMarine);
    }
}
