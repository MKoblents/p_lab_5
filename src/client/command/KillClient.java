package client.command;

import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import shared.dto.CommandRequest;

import java.io.IOException;

public class KillClient implements ClientCommand{
    private InputManager inputManager;
    public KillClient(InputManager inputManager){
        this.inputManager = inputManager;
    }

    @Override
    public CommandRequest execute(SideFlag flag) {
        String id;
        if (flag == SideFlag.SELF){
            id = inputManager.getLastString();
        }else {
            try {
                id = inputManager.getNewString();
            } catch (IOException e) {
                id = "";
            }
        }
        return RequestsFactory.withStringArg("kill_client", id);
    }
}
