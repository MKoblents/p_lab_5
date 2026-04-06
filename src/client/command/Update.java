package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import client.utils.Validator;
import shared.dto.CommandRequest;
import shared.models.SpaceMarine;

import java.io.IOException;

public class Update implements ClientCommand{
    private InputManager inputManager;
    public Update(InputManager inputManager){
        this.inputManager = inputManager;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        long id;
        if (flag == SideFlag.FORWARDED){
            try {
                id = inputManager.getNewLong();
            } catch (IOException e) {
                id = -1;
            }
        }else {
            id = inputManager.getLastLong();
        }
        if (id <= 0) {
            System.err.println("Error: Valid ID required for update");
            return null;
        }
        SpaceMarine marine = inputManager.getInputSpaceMarine();
        if (marine == null) {
            System.err.println("Error: Failed to parse SpaceMarine from XML");
            return null;
        }
        Validator.spaceMarineValidate(marine);
        return RequestsFactory.createTwoArgs("update", id, marine);
    }
}
