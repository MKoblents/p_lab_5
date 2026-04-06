package client.command;

import client.inputWorkers.InputManager;
import client.scripts.ScriptRunner;
import client.utils.SideFlag;
import shared.dto.CommandRequest;

import java.io.IOException;

public class ExecuteScript implements ClientCommand{
    private InputManager inputManager;
    private ScriptRunner runner;
    public ExecuteScript(InputManager inputManager,ScriptRunner runner){
        this.inputManager = inputManager;
        this.runner = runner;
    }
    @Override
    public CommandRequest execute(SideFlag flag) {
        String path;
        if (flag == SideFlag.FORWARDED) {
            try{
                path = inputManager.getNewString();
            } catch (IOException e) {
                path = null;
            }
        }else {
            path = inputManager.getLastPath();
        }
        if (path != null && !path.isEmpty()){
            runner.executeScript(path);
        }
        return null;
    }
}
