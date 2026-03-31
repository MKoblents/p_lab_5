package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import shared.dto.CommandRequest;

public class RemoveById implements ClientCommand{
    private InputManager inputManager;
    public RemoveById(InputManager inputManager){
        this.inputManager = inputManager;
    }
    @Override
    public CommandRequest execute() {
        long id = inputManager.getLastLong();
        if (id <= 0) {
            System.err.println("Error: Valid ID required for remove_by_id");
            return null;
        }
        return RequestsFactory.withLongArg("remove_by_id", id);
    }
}
