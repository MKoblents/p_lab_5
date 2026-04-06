package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import client.utils.Validator;
import shared.dto.CommandRequest;
import shared.models.SpaceMarine;

import java.io.IOException;

public class InsertAt implements ClientCommand{
    private InputManager inputManager;
    public InsertAt(InputManager inputManager){
        this.inputManager = inputManager;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        int index;
        if (flag == SideFlag.FORWARDED){
            try {
                index = inputManager.getNewInt();
            } catch (IOException e) {
                index = -1;
            }
        }else {
            index = inputManager.getLastInt();
        }
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
