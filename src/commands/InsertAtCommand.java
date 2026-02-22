package commands;

import inputWorkers.InputManager;
import manager.CollectionManager;
import manager.SpaceMarine;

public class InsertAtCommand implements Command{
    private CollectionManager collectionManager;
    private String helpInformation;
    private InputManager inputManager;
    public InsertAtCommand(CollectionManager collectionManager, InputManager inputManager){
        this.collectionManager = collectionManager;
        this.inputManager = inputManager;
    }

    @Override
    public String getHelpInformation() {
        return helpInformation;
    }

    @Override
    public void execute() {
        SpaceMarine spaceMarine = inputManager.getInputSpaceMarine();
        inputManager.getValidator().spaceMarineValidate(spaceMarine);
        collectionManager.addItem(inputManager.getLastInt(), spaceMarine);
    }
}
