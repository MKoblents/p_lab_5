package client.command;


import client.inputWorkers.InputManager;
import client.utils.RequestsFactory;
import client.utils.SideFlag;
import shared.dto.CommandRequest;
import shared.dto.UserInfo;

import java.io.IOException;

public class LogIn implements ClientCommand {
    private final InputManager inputManager;
    public LogIn(InputManager inputManager){
        this.inputManager = inputManager;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        try {
            System.out.println("Enter username: ");
            String username = inputManager.getNewString();
            System.out.println("Enter password:");
            String password = inputManager.getNewString();
            return RequestsFactory.creatLogRequest("log_in", new UserInfo(username,password));
        }catch (IOException e){
            return null;
        }
    }
}
