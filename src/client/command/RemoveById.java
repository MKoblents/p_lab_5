package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import shared.dto.CommandRequest;

import java.io.IOException;

public class RemoveById implements ClientCommand{
    private InputManager inputManager;
    public RemoveById(InputManager inputManager){
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
            System.err.println("Error: Valid ID required for remove_by_id");
            return null;
        }
        return RequestsFactory.withLongArg("remove_by_id", id);
    }
}
