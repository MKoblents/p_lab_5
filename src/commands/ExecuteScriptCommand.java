package commands;

import inputWorkers.InputManager;
import manager.CollectionManager;

public class ExecuteScriptCommand implements Command{
    private String helpInformation;
    private CollectionManager collectionManager;
    private InputManager inputManager;
    public ExecuteScriptCommand(CollectionManager collectionManager, InputManager inputManager){
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        String scriptPath = inputManager.getLastPath();

    }
}
